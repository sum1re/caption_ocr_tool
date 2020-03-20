package com.neo.caption.ocr.aspect;

import com.google.common.base.Stopwatch;
import com.google.gson.JsonSyntaxException;
import com.neo.caption.ocr.exception.InvalidMatNodesException;
import com.neo.caption.ocr.exception.ModuleException;
import com.neo.caption.ocr.exception.TessException;
import com.neo.caption.ocr.service.StageService;
import com.neo.caption.ocr.util.FxUtil;
import com.neo.caption.ocr.view.Toast;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.opencv.core.CvException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipException;

@Aspect
@Component
@Slf4j
public class AopExceptionAspect {

    private final StageService stageService;
    private final ResourceBundle resourceBundle;
    private final FxUtil fxUtil;

    private final static String INFO = "{} - {} in {} ms";
    private final static String ERROR = "Pointcut Class: {}\n" +
            "Pointcut Method: {}\n" +
            "ExceptionName: {}\n" +
            "ExceptionMsg: {}\n" +
            "ParamNames: {}\n" +
            "ParamClasses: {}\n" +
            "Values: {}\n" +
            "Full Stack: {}";

    private StringBuilder stringBuilder;
    private Stopwatch stopwatch;

    public AopExceptionAspect(StageService stageService, ResourceBundle resourceBundle, FxUtil fxUtil) {
        this.stageService = stageService;
        this.resourceBundle = resourceBundle;
        this.fxUtil = fxUtil;
    }

    @PostConstruct
    public void init() {
        this.stringBuilder = new StringBuilder(512);
    }

    @SuppressWarnings("EmptyMethod")
    @Pointcut("@annotation(com.neo.caption.ocr.aspect.AopException)")
    public void pointcut() {
        //do nothing
    }

    @Before("pointcut()")
    public void before() {
        stopwatch = Stopwatch.createStarted();
    }

    @After("pointcut()")
    public void after(JoinPoint joinPoint) {
        log.info(INFO,
                joinPoint.getTarget().getClass().getName(),
                joinPoint.getSignature().getName(),
                stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    /*
    Do not use 'Around' for catching global exceptions, which will lose the data from the pointcut.
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        return pjp.proceed();
    }
     */

    /**
     * When throw an exception
     */
    @AfterThrowing(value = "pointcut()", throwing = "throwable")
    public void afterThrowing(JoinPoint joinPoint, Throwable throwable) {
        stringBuilder.setLength(0);
        Arrays.stream(throwable.getStackTrace())
                .filter(stackTraceElement -> stackTraceElement.getClassName().contains("neo.caption"))
                .forEach(element -> stringBuilder.append("Declare: ").append(element.getClassName()).append("\t")
                        .append("Method: ").append(element.getMethodName()).append("\t")
                        .append("Line: ").append(element.getLineNumber()).append("\n"));
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        log.error(ERROR,
                joinPoint.getTarget().getClass().getName(),
                joinPoint.getSignature().getName(),
                throwable.getClass().getCanonicalName(),
                throwable.getMessage(),
                methodSignature.getParameterNames(),
                methodSignature.getParameterTypes(),
                joinPoint.getArgs(),
                stringBuilder.toString());
        Platform.runLater(() -> handler(stageService.getFocusedStage(), throwable));
    }

    private void handler(Stage stage, Throwable throwable) {
        if (stage == null) {
            return;
        }
        if (throwable instanceof InvalidMatNodesException) {
            Toast.makeToast(stage, resourceBundle.getString("snackbar.empty.mat.nodes"));
            return;
        }
        if (throwable instanceof FileNotFoundException || throwable instanceof NoSuchFileException) {
            Toast.makeToast(stage, resourceBundle.getString("snackbar.no.such.file"));
            return;
        }
        if (throwable instanceof OutOfMemoryError) {
            fxUtil.alert(stage,
                    resourceBundle.getString("alert.title.serious.error"),
                    null,
                    resourceBundle.getString("alert.content.oom"));
            return;
        }
        if (throwable instanceof UnsatisfiedLinkError) {
            fxUtil.alert(stage,
                    resourceBundle.getString("alert.title.serious.error"),
                    resourceBundle.getString("alert.header.unsatisfied.link"),
                    resourceBundle.getString("alert.content.unsatisfied.link")
            );
            return;
        }
        if (throwable instanceof AccessDeniedException) {
            fxUtil.alert(stage,
                    resourceBundle.getString("alert.title.serious.warning"),
                    null,
                    resourceBundle.getString("alert.content.access.denied"));
            return;
        }
        if (throwable instanceof NullPointerException) {
            fxUtil.alert(stage,
                    resourceBundle.getString("alert.title.serious.error"),
                    resourceBundle.getString("alert.header.null.pointer"),
                    resourceBundle.getString("alert.content.null.pointer"),
                    Collections.singletonList(throwable.getMessage()));
            return;
        }
        if (throwable instanceof TessException) {
            fxUtil.alert(stage,
                    resourceBundle.getString("alert.title.serious.error"),
                    null,
                    throwable.getMessage());
            return;
        }
        if (throwable instanceof JsonSyntaxException || throwable instanceof ZipException) {
            fxUtil.alert(stage,
                    resourceBundle.getString("alert.title.user.error"),
                    null,
                    resourceBundle.getString("alert.content.json.syntax"));
            return;
        }
        if (throwable instanceof ModuleException || throwable instanceof CvException) {
            fxUtil.alert(stage,
                    resourceBundle.getString("alert.title.user.error"),
                    null,
                    resourceBundle.getString("alert.content.module.profile"),
                    Collections.singletonList(throwable.getMessage()));
            return;
        }
        if (throwable instanceof ExecutionException) {
            //do nothing
            return;
        }
        if (throwable instanceof IOException) {
            fxUtil.alert(stage,
                    resourceBundle.getString("alert.title.serious.warning"),
                    null,
                    resourceBundle.getString("alert.content.io"));
            return;
        }
        fxUtil.alert(stage,
                resourceBundle.getString("alert.title.serious.error"),
                resourceBundle.getString("alert.header.exception"),
                resourceBundle.getString("alert.content.exception"));
    }
}
