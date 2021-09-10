const dotenv = require('dotenv');
dotenv.config();
const asymmetricEncryption = require("./asymmetricEncryption")
const express = require('express');
const cors = require('cors');
const fs = require("fs");
const app = express();
const port = 8080

// noinspection JSCheckFunctionSignatures
app.use(cors({
    origin: '*'
}));

app.use(express.urlencoded({
    extended: true
}))

function parseKeySet(text) {
    let byteArray = text.toString().replace(/]/, "").replace(/\[/, "").split(",");
    byteArray.splice(0, 6);
    let jsonString = ""
    byteArray.forEach(element => jsonString += String.fromCharCode(parseInt(element.trim())))
    let json = JSON.parse(jsonString)
    return {primaryKeyId: json.primaryKeyId, value: json.key[0].keyData.value}
}

function stringToByteArray(string) {
    let myBuffer = [-84, -19, 0, 5, 119, -18];
    const buffer = Buffer.from(string, 'utf8');
    for (let i = 0; i < buffer.length; i++) {
        myBuffer.push(buffer[i]);
    }
    myBuffer.push(10)
    return myBuffer
}

function constructKeyset(keysetValues) {
    keysetValues = JSON.parse(keysetValues)
    let keyset = {
        "primaryKeyId": keysetValues.primaryKeyId,
        "key": [{
            "keyData": {
                "typeUrl": "type.googleapis.com/google.crypto.tink.AesGcmKey",
                "value": keysetValues.value,
                "keyMaterialType": "SYMMETRIC"
            }, "status": "ENABLED", "keyId": keysetValues.primaryKeyId, "outputPrefixType": "TINK"
        }]
    }
    let jsonString = JSON.stringify(keyset)
    return stringToByteArray(jsonString)
}

app.post('/encrypt', function (req, res) {
    res.setHeader('Content-Type', 'application/json');
    let keysetValues = parseKeySet(req.body.text)
    asymmetricEncryption.encrypt(Buffer.from(JSON.stringify(keysetValues), 'utf8'))
        .then(cipher => {
            res.end(JSON.stringify({cipher}))
        })
        .catch(err => {
            console.log("There was an error", err)
        })
});
app.post('/decrypt', function (req, res) {
    res.setHeader('Content-Type', 'application/json');
    let byteArray = JSON.parse(req.body.cipher).cipher.data
    asymmetricEncryption.decrpyt(Buffer.from(byteArray)).then(keysetValues => {
        let keyset = constructKeyset(keysetValues)
        return res.end(JSON.stringify(keyset))
    }).catch(err => {
        console.log("There was an error", err)
    })
});

app.post('/isUserExists', function (req, res) {
    res.setHeader('Content-Type', 'application/json');
    const data = require("./masterPassword.json")
    return res.end((data[req.body.email] !== undefined).toString())
});

app.post('/checkCredentials', function (req, res) {
    res.setHeader('Content-Type', 'application/json');
    const data = require("./masterPassword.json")
    return res.end((data[req.body.email] === req.body.password).toString())
});

app.post('/setUser', function (req, res) {
    res.setHeader('Content-Type', 'application/json');
    const data = require("./masterPassword.json")
    data[req.body.email] = req.body.password
    fs.writeFile("masterPassword.json", JSON.stringify(data), err => {
        if (err) {
            console.log(err)
            return res.end(JSON.stringify({result: false}))
        } else {
            return res.end(JSON.stringify({result: true}))
        }
    });
});

app.post('/delUser', function (req, res) {
    res.setHeader('Content-Type', 'application/json');
    const data = require("./masterPassword.json")
    delete data[req.body.email];
    fs.writeFile("masterPassword.json", JSON.stringify(data), err => {
        if (err) {
            console.log(err)
            return res.end(JSON.stringify({result: false}))
        } else {
            return res.end(JSON.stringify({result: true}))
        }
    });
});

app.listen(port, () => {
    console.log(`Password-Manager Server listening at http://localhost:${port}`)
})






