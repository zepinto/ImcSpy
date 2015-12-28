# ImcSpy
Utility application for monitoring IMC communications

# Introduction
ImcSpy helps users capture the IMC data flowing in the local network by using libpcap library.

# Requirements
  - Java 1.6+
  - libpcap
  - Apache ant (for compilation)

# Compilation
In the folder where you cloned this project run:
```
ant
```
# Usage
Notice that your system may require super-user previledges to start capturing network data.
```
sudo java -jar ImcSpy.jar <interfaces to listen>
```
