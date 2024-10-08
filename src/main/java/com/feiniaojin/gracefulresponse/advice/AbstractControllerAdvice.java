package com.feiniaojin.gracefulresponse.advice;

import com.feiniaojin.gracefulresponse.advice.lifecycle.exception.*;
import com.feiniaojin.gracefulresponse.data.Response;
import org.springframework.http.ResponseEntity;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 抽象的异常处理基类
 *
 * @author qinyujie
 */
public abstract class AbstractControllerAdvice {

    /**
     * 执行处理之前的判断，只有所有的判断都生效，才会进行异常处理
     */
    private CopyOnWriteArrayList<ControllerAdvicePredicate> predicates = new CopyOnWriteArrayList<>();

    private RejectStrategy rejectStrategy = new DefaultRejectStrategyImpl();

    private BeforeControllerAdviceProcess beforeControllerAdviceProcess;

    private AfterControllerAdviceProcess afterControllerAdviceProcess;

    private ControllerAdviceProcessor controllerAdviceProcessor;

    private ControllerAdviceHttpProcessor controllerAdviceHttpProcessor;

    public ResponseEntity<Response> exceptionHandler(Throwable throwable) {
        //默认认为只要捕获到的，都要进行处理
        boolean hit = true;
        CopyOnWriteArrayList<ControllerAdvicePredicate> pList = this.predicates;
        for (ControllerAdvicePredicate predicateBeforeHandle : pList) {
            if (!predicateBeforeHandle.test(throwable)) {
                hit = false;
                break;
            }
        }

        //不需要处理，由RejectHandler决定该如何解决，默认为往后抛
        if (!hit) {
            return rejectStrategy.call(throwable);
        }

        //执行之前做一下进行回调，可以用于执行前的日志打印
        if (Objects.nonNull(beforeControllerAdviceProcess)) {
            beforeControllerAdviceProcess.call(throwable);
        }

        //处理异常，加工出来Response
        Response response = controllerAdviceProcessor.process(throwable);

        //得到Response后的处理，可能需要打印日志
        if (Objects.nonNull(afterControllerAdviceProcess)) {
            afterControllerAdviceProcess.call(response, throwable);
        }

        //HTTP的处理收敛到这里，处理HTTP 状态码、Header
        ResponseEntity<Response> responseEntity = controllerAdviceHttpProcessor.process(response, throwable);

        return responseEntity;
    }

    public CopyOnWriteArrayList<ControllerAdvicePredicate> getPredicates() {
        return predicates;
    }

    public void setPredicates(CopyOnWriteArrayList<ControllerAdvicePredicate> predicates) {
        this.predicates = predicates;
    }

    public ControllerAdviceProcessor getControllerAdviceProcessor() {
        return controllerAdviceProcessor;
    }

    public void setControllerAdviceProcessor(ControllerAdviceProcessor controllerAdviceProcessor) {
        this.controllerAdviceProcessor = controllerAdviceProcessor;
    }

    public RejectStrategy getRejectStrategy() {
        return rejectStrategy;
    }

    public void setRejectStrategy(RejectStrategy rejectStrategy) {
        this.rejectStrategy = rejectStrategy;
    }

    public BeforeControllerAdviceProcess getBeforeAdviceProcess() {
        return beforeControllerAdviceProcess;
    }

    public void setBeforeControllerAdviceProcess(BeforeControllerAdviceProcess beforeControllerAdviceProcess) {
        this.beforeControllerAdviceProcess = beforeControllerAdviceProcess;
    }

    public AfterControllerAdviceProcess getAfterControllerAdviceProcess() {
        return afterControllerAdviceProcess;
    }

    public void setAfterControllerAdviceProcess(AfterControllerAdviceProcess afterControllerAdviceProcess) {
        this.afterControllerAdviceProcess = afterControllerAdviceProcess;
    }

    public BeforeControllerAdviceProcess getBeforeControllerAdviceProcess() {
        return beforeControllerAdviceProcess;
    }

    public ControllerAdviceHttpProcessor getControllerAdviceHttpProcessor() {
        return controllerAdviceHttpProcessor;
    }

    public void setControllerAdviceHttpProcessor(ControllerAdviceHttpProcessor controllerAdviceHttpProcessor) {
        this.controllerAdviceHttpProcessor = controllerAdviceHttpProcessor;
    }
}
