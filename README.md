# Aop-log  
modified version of [this](https://github.com/nickvl/aop-logging).  

## Example  
### code  
  
```java
@LogInteraction  
public interface AuthorizationTokenService {  
  
    public String createToken(RapidAuthenticatedPrincipal principal);  
    public P parseToken(String token) throws BadTokenException, BadCredentialsException;  
}  
```  
  
### log output  
```
2020-12-16 13:03:00.340 DEBUG 5691 --- [           main] .a.s.t.RapidJwtAuthorizationTokenService :   
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++   
     ->  CALLING: parseToken(token=eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhdXRoIiwic3ViIjoiYWRtaW5AZXhhbXBsZS5jb20iLCJleHBpcmVkIjoxNjA4OTg...)  ,Thread: 1    
__________________________________________________________________________________________________________________________   
  
2020-12-16 13:03:00.370 DEBUG 5691 --- [           main] c.g.v.springrapid.auth.util.RapidJwt     : Check if token is expired...  
2020-12-16 13:03:00.371 DEBUG 5691 --- [           main] c.g.v.springrapid.auth.util.RapidJwt     : Expiration time = Sat Dec 26 13:02:59 UTC 2020. Current time = Wed Dec 16 13:03:00 UTC 2020  
2020-12-16 13:03:00.379 DEBUG 5691 --- [           main] c.g.v.springrapid.auth.util.RapidJwt     : Token not expired.  
2020-12-16 13:03:00.388 DEBUG 5691 --- [           main] c.g.v.springrapid.auth.util.RapidJwt     : Check if token is obsolete...  
2020-12-16 13:03:00.388 DEBUG 5691 --- [           main] c.g.v.springrapid.auth.util.RapidJwt     : Token issued at: Wed Dec 16 13:02:59 UTC 2020, must be issued after: Wed Dec 16 13:02:56 UTC 2020  
2020-12-16 13:03:00.389 DEBUG 5691 --- [           main] c.g.v.springrapid.auth.util.RapidJwt     : Token is not obsolete.  
2020-12-16 13:03:00.389 DEBUG 5691 --- [           main] .a.s.t.RapidJwtAuthorizationTokenService :    
__________________________________________________________________________________________________________________________  
     <-  RETURNING: parseToken { RapidAuthenticatedPrincipal(super=RapidAuthenticatedPrincipal(name=admin@example.com, roles=[ROLE_ADMIN] ...) , Thread 1  
==========================================================================================================================  
```
  
## Additional features  
* works on abstract classes and interfaces and with any type of spring proxy  
* config annotation to configure class level logging  
* automatic identation of log statements to show call hierachry  
* add padding and thread-info  
* possible to log bean-name    
* finegrained control over logging on each level of the class- (and interface-)hierarchy   

## Removed features  
* repeatable log annotations 
* toggling of args/result logging -> it will always log args and result  
  
## Include  
### Maven  
```code
<repositories>  
    <repository>  
        <id>jitpack.io</id>  
        <url>https://jitpack.io</url>  
    </repository>  
</repositories>  
  
<dependency>  
    <groupId>com.github.vincemann</groupId>  
    <artifactId>aop-log</artifactId>  
    <version>1.0.0-SNAPSHOT.3</version>  
</dependency>  
```  
 
### Gradle  
   
```code
repositories {  
    jcenter()  
    maven { url "https://jitpack.io" }  
}  
dependencies {  
     implementation 'com.github.vincemann:aop-log:1.0.0-SNAPSHOT.3'  
}  
```
 

## Config  
```java  
@Configuration  
@EnableAspectJAutoProxy(proxyTargetClass = true)  
public class AopLogConfiguration {  
    
    private static final boolean SKIP_NULL_FIELDS = true;  
    private static final boolean FORCE_REFLECTION = false;  
    private static final int CROP_THRESHOLD = 7;  
    private static final Set<String> EXCLUDE_SECURE_FIELD_NAMES = Sets.newHashSet("password");  
  
    @ConditionalOnMissingBean(ProxyAwareAopLogger.class)  
    @Bean  
    public ProxyAwareAopLogger aopLogger() {  
        ProxyAwareAopLogger aopLogger = new ProxyAwareAopLogger(new TypeHierarchyAnnotationParser(),new InvocationDescriptorFactoryImpl());  
        aopLogger.setLogAdapter(new ThreadAwareIndentingLogAdapter(SKIP_NULL_FIELDS, CROP_THRESHOLD, EXCLUDE_SECURE_FIELD_NAMES,FORCE_REFLECTION));  
        return aopLogger;  
    }  
  
}  
```
