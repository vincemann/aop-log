# aop-log  
modified version of [this](https://github.com/nickvl/aop-logging).  

## additional features  
* works on abstract classes and interfaces and with any type of spring proxy  
* Config annotation to configure class level logging  
* automatic identation of log statements to show call hierachry  
* add padding and thread-info  

## removed features  
* repeatable log annotations 
* toggling of args/result logging -> it will always log args and result  

## config  
```java  
@Configuration  
@EnableAspectJAutoProxy(proxyTargetClass = true)  
@Slf4j  
public class AopLogConfiguration {  
  
    private static final boolean SKIP_NULL_FIELDS = true;  
    private static final int CROP_THRESHOLD = 7;  
    private static final Set<String> EXCLUDE_SECURE_FIELD_NAMES = Collections.<String>emptySet();  
  
    @Bean  
    public AOPLogger aopLogger() {  
        AOPLogger aopLogger = new AOPLogger(new HierarchicalAnnotationParser());  
        aopLogger.setLogAdapter(new ThreadAwareIndentingLogAdapter(SKIP_NULL_FIELDS, CROP_THRESHOLD, EXCLUDE_SECURE_FIELD_NAMES));  
        return aopLogger;  
    }  
}  
```
