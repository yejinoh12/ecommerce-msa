package com.common.exception;

/**
 * Throwable : 최 상위 예외 객체, 하위에 Exception,Error
 * - Exception(애플리케이션 로직에서 사용할 수 있는 실질 적인 최상위 예외)
 *      - SQLException, IOException, 컴파일러가 체크하는 체크 예외 -> 예외를 처리하거나, 던지거나
 *      - RuntimeException : 컴파일러가 체크하지 않는 언체크 에러 -> 예외를 처리하지 않아도, throws 키워드 생략가능
 * - Error(메모리 부족, 심각한 시트템 오류로 애플리케이션에서 복구가 불가능한 시스템 예외)
 *
 * 상속관계에서 부모 타입은 자식을 잡을 수 있음, Throwable 예외를 잡으면 Error 예외까지 잡게 되므로
 * Throwable 예외를 잡으면 안됨.
 */

/**
 * 예외 계층화 : 예외는 객체다. 부모 예외를 잡거나 던지면 자식 예외도 함께 잡거나 던질 수 있다.
 * BaseBizException(부모 예외)을 잡으면 그 하위에 있는 자식 예외도 함께 잡거나 던질 수 있기 때문이다. / 자식 예외를 따로 처리할 수도 있음
 */

public class BaseBizException extends RuntimeException {

    public BaseBizException(String errorMessage) {
        super(errorMessage);
    }
}
