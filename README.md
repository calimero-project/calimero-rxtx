# Calimero RXTX provider [![CI with Gradle](https://github.com/calimero-project/calimero-rxtx/actions/workflows/gradle.yml/badge.svg)](https://github.com/calimero-project/calimero-rxtx/actions/workflows/gradle.yml)

Provider for serial access to KNX networks using the RXTX (or any compatible) library.

All Calimero build artifacts are directly downloadable from Maven repositories.
Simply put the provider on the class path used by Calimero. 

This provider uses `System.Logger` for logging.

## Building from Source

~~~ sh
git clone https://github.com/calimero-project/calimero-rxtx.git
~~~

With Gradle:

```
gradle build
```

With Maven:

```
mvn clean build
```

## Dependencies

- `calimero-core`
- `com.neuronrobotics:nrjavaserial`
