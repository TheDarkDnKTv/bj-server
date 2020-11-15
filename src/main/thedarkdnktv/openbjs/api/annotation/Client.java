package thedarkdnktv.openbjs.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author TheDarkDnKTv <br>
 * @param cleintId is unique id of used client
 * @param vesion is a API version
 * This annotation marks a class, which will represents as Client in case of usage one-runtime clint-server system
 */

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Client {
	String versionAPI();
	String clientId();
}
