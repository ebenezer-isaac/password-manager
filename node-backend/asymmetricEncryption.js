const projectId = 'premium-bloom-319904';
const locationId = 'global';
const keyRingId = 'password-manager';
const keyId = 'password-manager';
const versionId = '1';
const {KeyManagementServiceClient} = require('@google-cloud/kms');
const client = new KeyManagementServiceClient();
const versionName = client.cryptoKeyVersionPath(
    projectId,
    locationId,
    keyRingId,
    keyId,
    versionId
);
const crc32c = require('fast-crc32c');

async function decrypt(ciphertext) {

    const ciphertextCrc32c = crc32c.calculate(ciphertext);

    async function decryptAsymmetric() {
        const [decryptResponse] = await client.asymmetricDecrypt({
            name: versionName,
            ciphertext: ciphertext,
            ciphertextCrc32c: {
                value: ciphertextCrc32c,
            },
        });
        if (!decryptResponse.verifiedCiphertextCrc32c) {
            throw new Error('AsymmetricDecrypt: request corrupted in-transit');
        }
        if (
            crc32c.calculate(decryptResponse.plaintext) !==
            Number(decryptResponse.plaintextCrc32c.value)
        ) {
            throw new Error('AsymmetricDecrypt: response corrupted in-transit');
        }
        return decryptResponse.plaintext.toString();
    }

    return decryptAsymmetric();
}

async function encrypt(plaintextBuffer) {
    async function encryptAsymmetric() {
        const [publicKey] = await client.getPublicKey({
            name: versionName,
        });
        if (publicKey.name !== versionName) {
            throw new Error('GetPublicKey: request corrupted in-transit');
        }
        if (crc32c.calculate(publicKey.pem) !== Number(publicKey.pemCrc32c.value)) {
            throw new Error('GetPublicKey: response corrupted in-transit');
        }
        const crypto = require('crypto');
        return crypto.publicEncrypt(
            {
                key: publicKey.pem,
                oaepHash: 'sha256'
            },
            plaintextBuffer
        );
    }

    return encryptAsymmetric();
}

module.exports.encrypt = encrypt;
module.exports.decrpyt = decrypt;