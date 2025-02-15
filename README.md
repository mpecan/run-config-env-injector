# Run Configuration Environment Injector

[![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mpecan/rrun-config-env-injector)
[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/v/si.pecan.aws-codeartifact-env-provider.svg?style=for-the-badge&logo=jetbrains&logoColor=white)](https://plugins.jetbrains.com/plugin/com.github.mpecan.runconfigenvinjector)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/si.pecan.aws-codeartifact-env-provider.svg?style=for-the-badge)](https://plugins.jetbrains.com/plugin/com.github.mpecan.runconfigenvinjector)

<!-- Plugin description -->
An IntelliJ IDEA plugin that enables dynamic injection of environment variables into your run configurations. Originally designed for AWS CodeArtifact authentication token injection, it has evolved to support multiple environment variable sources, making it a versatile tool for managing environment variables in your IDE run configurations.

## Features

- Inject environment variables from multiple sources:
  - AWS CodeArtifact authentication tokens
  - Single value files
  - Structured files (multiple values with selective injection)
- Supports both Maven and Gradle run configurations
- Configurable per run configuration
- Easy-to-use settings interface

## Why Use This Plugin?

- **AWS CodeArtifact Integration**: Automatically inject authentication tokens for private Maven repositories
- **Flexible Configuration**: Support for multiple environment variable sources in a single configuration
- **Run Configuration Specific**: Apply different environment variables to different run configurations
- **File-Based Sources**: Read environment variables from:
  - Simple text files (single value)
  - Structured files (JSON, YAML, Properties)
- **IDE Integration**: Seamless integration with IntelliJ IDEA's run configuration system
- **Framework Support**: Works with both Maven and Gradle projects

## Requirements

- IntelliJ IDEA (Community or Ultimate) 2022.2 or later
- Java 11 or later
- For AWS CodeArtifact features:
  - Valid AWS credentials
  - Appropriate AWS CodeArtifact repository permissions
- Supported Build Tools:
  - Maven (requires IntelliJ IDEA Maven integration)
  - Gradle (requires IntelliJ IDEA Gradle integration)

## Technical Details

This plugin integrates with:
- IntelliJ Platform SDK
- Maven Integration Plugin
- Gradle Integration Plugin
- AWS SDK for CodeArtifact operations

The plugin runs as a Run Configuration Extension, which allows it to modify environment variables just before your run configurations execute.
<!-- Plugin description end -->
## Installation

Install directly from the JetBrains Marketplace:

1. Open IntelliJ IDEA
2. Go to Settings/Preferences → Plugins
3. Search for "Run Configuration Environment Provider"
4. Click Install and restart IDE when prompted

## Usage

1. Open Settings/Preferences → Build, Execution, Deployment → Env Provider
2. Add a new environment variable configuration
3. Select the source type (CodeArtifact, File, or Structured File)
4. Configure the environment variable name and source settings
5. Enable for specific run configuration types (Maven/Gradle)

The plugin will automatically inject the configured environment variables when you run your configurations.

### AWS CodeArtifact Setup

1. Configure your AWS credentials using standard AWS credential providers
2. In the plugin settings, configure:
  - Region
  - Domain
  - Repository
  - Domain owner
3. The plugin will automatically manage authentication tokens

### File Source Setup

1. For single value files:
  - Select "File" as the source type
  - Choose your file path
  - Specify the environment variable name

2. For structured files:
  - Select "Structured File" as the source type
  - Choose your file path
  - Specify the JSON path or property key
  - Configure the target environment variable name

## Configuration

### Plugin Settings Location
The plugin settings can be found in:
Settings/Preferences → Build, Execution, Deployment → Env Provider

### Settings Options
1. **Environment Variable Sources**
  - AWS CodeArtifact Configuration
    - Region
    - Domain
    - Repository
    - Domain Owner
  - File Source Configuration
    - File path
    - Variable name
  - Structured File Configuration
    - File path
    - Path/Key selector
    - Variable name

2. **Run Configuration Integration**
  - Enable/disable per configuration type
  - Variable override settings
  - Source priority settings

### Best Practices
- Use relative paths when possible for file sources
- Set meaningful variable names that won't conflict
- Configure AWS credentials through standard AWS credential chain
- Review and test variables in the run configuration environment variables preview

## Troubleshooting

### AWS CodeArtifact Issues
- Ensure AWS credentials are properly configured in your environment
- Verify you have the correct permissions in AWS IAM for CodeArtifact operations
- Check if the domain, repository, and region settings are correct
- Look for error messages in the IDE's event log (Help -> Show Log in Explorer)

### File Source Issues
- Verify file paths are accessible to the IDE
- For structured files, ensure the JSON path or property key is correct
- Check file permissions
- For relative paths, they are resolved relative to the project root

### Run Configuration Issues
- Make sure the plugin is enabled for your specific run configuration type
- Verify the environment variable name doesn't conflict with system variables
- Check if the run configuration has the latest changes by refreshing it

If you encounter any other issues, please report them on our [GitHub repository](https://github.com/mpecan/run-config-env-loader/issues).

## Development Setup

To set up the development environment:

1. Clone the repository
```bash
git clone https://github.com/mpecan/run-config-env-loader.git
cd run-config-env-loader
```

2. Import the project into IntelliJ IDEA:
  - Open IntelliJ IDEA
  - Choose "Open" and select the project directory
  - Wait for the Gradle sync to complete

3. Build the plugin:
```bash
./gradlew build
```

4. Run the plugin in a development instance:
```bash
./gradlew runIde
```

## Future Features

- Support for additional environment variable sources:
  - AWS Secrets Manager integration
  - Environment variable templates
  - Docker environment files
- Enhanced variable validation and preview
- Support for more IDE run configuration types
- Variable substitution and templating
- Improved UI for managing multiple configurations

## Contributing

We welcome contributions of all kinds! Here's how you can help:

### Development Process
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Run tests locally (`./gradlew test`)
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to your branch (`git push origin feature/amazing-feature`)
7. Create a Pull Request

### Areas for Contribution
- New environment variable sources
- Improved error handling and user feedback
- Documentation improvements
- Bug fixes and performance improvements
- UI/UX enhancements
- Test coverage

Please ensure your PR:
- Follows the existing code style
- Includes appropriate tests
- Updates documentation as needed
- Describes the changes in detail

## License

MIT License

Copyright (c) 2024 Matjaz Domen Pecan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.