package com.gmbbd.checkMate.model;

import lombok.AllArgsConstructor;
import lombok.Data;



    @Data                       // getter/setter, toString generating
    @AllArgsConstructor         // Constructor generating
    public class ErrorResponse {

        private String message; // the body of the error message (will be return to client)
    }


