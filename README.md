# aop-log  
modified version of [this](https://github.com/nickvl/aop-logging).  

## additional features  
* works on abstract classes and interfaces and with any type of spring proxy  
* Config annotation to configure class level logging  
* automatic identation of log statements to show call hierachry  
* add padding and thread-info  
* finegrained control over logging on each level of the class- (and interface-)hierarchy   

## removed features  
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
 

## config  
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
