package com.neo.caption.ocr.aspect;

import java.lang.annotation.*;

/**
 * Add to the method that throwing the exception,
 * cannot be used on the 'BaseController' or the class that implements it.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AopException {

}
