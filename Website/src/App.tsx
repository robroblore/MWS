import { useState } from 'react'
import './App.css'

function App() {
    const [inputValue, setInputValue] = useState("")
    const [selectedMethod, setSelectedMethod] = useState('binary');
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [downloadedFile, setDownloadedFile] = useState<File | null>(null);

    const server = "http://178.82.92.132:6969"

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
        console.log(data)
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

    const readImageFromServer = async () => {
        const response = await fetch(server, {
            method: 'GET',
            headers: {
                'Content-Type': 'image/*',
                'X-StorageMethod': selectedMethod,
            }
        })
        // Get raw bytes
        const arrayBuffer = await response.arrayBuffer();

        // Optionally use headers to restore metadata
        const contentType = response.headers.get("Content-Type") ?? "image/png";
        const fileName = response.headers.get("X-FileName") ?? "downloaded-image";

        // Rebuild File object
        const file = new File([arrayBuffer], fileName, { type: contentType });

        setDownloadedFile(file);
    }

    const sendImageToServer = async () => {
        if (!selectedFile) {
            alert("Please select an image first!")
            return
        }

        const response = await fetch(server, {
            method: "POST",
            body: selectedFile,
            headers: {
                'Content-Type': selectedFile.type,
                'X-FileName':selectedFile.name,
                'X-FileSize':selectedFile.size.toString(),
                'X-StorageMethod': selectedMethod,
            }
        })

        const data = await response.text()
        console.log(data)
        alert("Image uploaded successfully!")
    }

    return (
        <>
            <h1>Minecraft WebServer</h1>
            <div className="card" style={{ display: "flex", gap: "20px" }}>
                {/* Left side: text input */}
                <div style={{ flex: 1 }}>
                    <input
                        name="myInput"
                        value={inputValue}
                        onChange={(e) => setInputValue(e.target.value)}
                    />

                    <p>You typed: {inputValue}</p>

                    <select value={selectedMethod} onChange={e => setSelectedMethod(e.target.value)}>
                        <option value="binary">Binary</option>
                        <option value="hexa">Hexa</option>
                    </select>

                    <button onClick={sendToServer}>Send to server</button>
                    <button onClick={readFromServer}>Read from server</button>
                </div>

                {/* Right side: image upload */}
                <div style={{ flex: 1 }}>
                    <input
                        type="file"
                        accept="image/*"
                        onChange={(e) => setSelectedFile(e.target.files?.[0] ?? null)}
                    />
                    <button onClick={sendImageToServer}>Send image</button>
                    <button onClick={readImageFromServer}>Read image</button>
                    {downloadedFile && (
                        <div style={{ marginTop: "10px" }}>
                            <p>Downloaded: {downloadedFile.name} ({downloadedFile.size} bytes)</p>
                            <img
                                src={URL.createObjectURL(downloadedFile)}
                                alt="Downloaded from server"
                                style={{ maxWidth: "100%", borderRadius: "8px" }}
                            />
                        </div>
                    )}
                </div>
            </div>
        </>
    )
}

// @ts-ignore
export default App
