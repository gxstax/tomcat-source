package com.ant.customer.exception;

/**
 * MIT License
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * </p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * @author Ant
 * @since 2023/1/30 19:35
 **/
public class TaskDoNotExecutedException extends RuntimeException {

    public TaskDoNotExecutedException(String message) {
        super(message);
    }

    public TaskDoNotExecutedException(String message, Throwable cause) {
        super(message, cause);
    }
}
