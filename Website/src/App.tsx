import { useState } from 'react'
import './App.css'

function App() {
    const [inputValue, setInputValue] = useState("")
    const [selectedMethod, setSelectedMethod] = useState('binary');

    const server = "http://86.220.127.86:6969"

    const sendToServer = async () => {
        const response = await fetch(server, {
            method: 'POST',
            body: inputValue,
            headers: {
                'Content-Type': 'text/plain',
                'X-StorageMethod': selectedMethod,
            },
        })
        const data = await response.text()
        // alert(data)
    }

    const readFromServer = async () => {
        const response = await fetch(server, {
            method: 'GET',
            headers: {
                'Content-Type': 'text/plain',
                'X-StorageMethod': selectedMethod,
            }
        })
        const data = await response.text()
        alert(data)
    }

    return (
        <>
            <h1>Minecraft WebServer</h1>
            <div className="card">

                <input

                    name="myInput"

                    value={inputValue} // 👈 binds input to state

                    onChange={(e) => setInputValue(e.target.value)} // 👈 updates state

                />

                <p>You typed: {inputValue}</p>

                <select value={selectedMethod} onChange={e => setSelectedMethod(e.target.value)}>
                    <option value="binary">Binary</option>
                    <option value="hexa">Hexa</option>
                </select>

                <button onClick={sendToServer}>Send to server</button>
                <button onClick={readFromServer}>Read from server</button>
            </div>
        </>
    )
}

// @ts-ignore
export default App
