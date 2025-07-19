# Stego-Tool

A modern JavaFX desktop application for encoding and decoding secret messages using steganography and encryption techniques. Supports multiple encoding steps, including zero-width characters, Base64, emoji, and password-based encryption.

## Features

- **Zero-width encoding:** Hide messages using invisible Unicode characters.
- **Base64 encoding:** Standard Base64 transformation.
- **Emoji encoding:** Encode data as a sequence of emojis.
- **Crypto (AES):** Encrypt messages with a password (AES-256, PBKDF2).
- **Pipeline:** Chain multiple steps for layered security.
- **User-friendly GUI:** Built with JavaFX and FXML, with a dark theme.
- **Clipboard integration:** Copy encoded/decoded results easily.

## Screenshots

> _Add screenshots here if available (e.g., selection screen, encode/decode screen)._

## Getting Started

### Prerequisites

- **Java 17 or newer** (JavaFX 17+ is recommended)
- **IntelliJ IDEA** (Community or Ultimate edition)

### Project Structure

```
Stego-Tool/
├── src/
│   └── com/yourorg/stegoapp/...
├── test/
│   └── com/yourorg/stegoapp/...
├── resources/
│   ├── views/
│   │   ├── selection.fxml
│   │   └── stego.fxml
│   └── styles/
│       └── dark-theme.css
└── README.md
```

### Running the Application

1. **Clone the repository:**
   ```
   git clone https://github.com/yourorg/Stego-Tool.git
   cd Stego-Tool
   ```

2. **Open in IntelliJ IDEA:**
   - Choose **File > Open...** and select the `Stego-Tool` directory.
   - IntelliJ will detect the `src` and `test` folders automatically.

3. **Configure JavaFX (if needed):**
   - Download JavaFX SDK from [https://openjfx.io/](https://openjfx.io/).
   - In IntelliJ, go to **Project Structure > Libraries** and add the JavaFX SDK `lib` directory.
   - Add VM options for running:
     ```
     --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
     ```

4. **Run the App:**
   - Right-click `com.yourorg.stegoapp.gui.MainApp` and select **Run 'MainApp.main()'**.

### Running Tests

- Right-click the `test` directory or any test class and select **Run 'All Tests'**.
- Tests use JUnit 5 and cover all core encoding/decoding logic.

## Usage

1. **Select a stego/encryption step** from the main menu.
2. **Enter your message** and configure any options (e.g., password for Crypto, cover text for Zero-width).
3. **Encode** to hide your message, or **Decode** to reveal a hidden message.
4. **Copy** results to the clipboard with a single click.

## Supported Steps

| Step Type   | Description                                 | Options         |
|-------------|---------------------------------------------|-----------------|
| Zero-width  | Invisible Unicode encoding                  | Cover text      |
| Base64      | Standard Base64 encoding                    | None            |
| Emoji       | Encodes bytes as emoji pairs                | None            |
| Crypto      | AES-256 encryption with password            | Password        |

## Customization

- **Add new steps:** Implement the `StegoStep` interface and register in `StegoFactory`.
- **Change theme:** Edit `resources/styles/dark-theme.css`.

## Troubleshooting

- **JavaFX errors:** Ensure the JavaFX SDK is properly configured in IntelliJ and VM options are set.
- **Unsupported Java version:** Use Java 17 or newer.
- **UI not loading:** Check that FXML and CSS files are in the correct `resources` subfolders.

## License

MIT License. See [LICENSE](LICENSE) for details.

## Credits

- Developed by [Your Name/Team].
- Emoji icons © respective creators.

---
