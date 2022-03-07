# API Proxy

A small API proxy using a web browser to perform calls. 
Can be used to restore API connectivity when application is behind a blocking SSO enterprise authentication.

This proxy will lauch a complete Web browser (Firefox or chrome) and will use it to access the API. 

## Usage

### Compile 

Using maven

```shell
mvn install
```

proxy is in 

```shell
target/api-proxy-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

### Install Driver 

Install browser driver in the same directory as the jar file.

  - [chromedriver (prefered)](https://chromedriver.chromium.org/downloads)
  - [geckodriver](https://github.com/mozilla/geckodriver/releases)

### Config 
Rename config.properties.sample into config.properties

```properties
# The target API Server
targetHost=https://target.server.com

# host name ignored (auth page)
loginHost=https://auth.server.com

# Listening port
port=8080

# Accept only local connections
bind=localhost
```


### Launch

```shell
java -jar api-proxy-1.0.0-SNAPSHOT-jar-with-dependencies
```


## License

Apache 2

