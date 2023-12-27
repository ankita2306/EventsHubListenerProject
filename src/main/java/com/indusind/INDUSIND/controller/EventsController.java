/*
package com.indusind.INDUSIND.controller;

import com.indusind.INDUSIND.service.EventsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/events")
public class EventsController {

    @Autowired
    private EventsService eventsService;

    @GetMapping("/consume-phone-events")
    public ResponseEntity<Object> consumePhoneBusinessEvents() {
       // eventsService.consumePhoneBusinessEvents();
        return ResponseEntity.status(HttpStatus.OK).body(eventsService.consumePhoneBusinessEvents());
    }

    @GetMapping("/consume-closed-accounts-events")
    public ResponseEntity<Object> consumeClosedAccountsBusinessEvents() {
        return ResponseEntity.status(HttpStatus.OK).body(eventsService.consumeClosedAccountsBusinessEvents());
    }

    @GetMapping("/consume-accounts-status-change-events")
    public ResponseEntity<Object> consumeAccountStatusChangeBusinessEvents() {
        return ResponseEntity.status(HttpStatus.OK).body(eventsService.consumeAccountStatusChangeBusinessEvents());
    }

    @GetMapping("/consume-new-customer-onboarded-events")
    public ResponseEntity<Object> consumeNewCustomerOnboardedBusinessEvents() {
        return ResponseEntity.status(HttpStatus.OK).body(eventsService.consumeNewCustomerOnboardedBusinessEvents());
    }

    @GetMapping("/consume-new-account-created-events")
    public ResponseEntity<Object> consumeNewAccountCreatedBusinessEvents() {
        return ResponseEntity.status(HttpStatus.OK).body(eventsService.consumeNewAccountCreatedBusinessEvents());
    }

    @GetMapping("/consume-cifId-change-on-account-events")
    public ResponseEntity<Object> consumeCifIdChangeOnAccountsBusinessEvents() {
        return ResponseEntity.status(HttpStatus.OK).body(eventsService.consumeCifIdChangeOnAccountsBusinessEvents());
    }

    @GetMapping("/consume-ucic-reference-events")
    public ResponseEntity<Object> consumeUcicReferenceBusinessEvents() {
        return ResponseEntity.status(HttpStatus.OK).body(eventsService.consumeUcicReferenceBusinessEvents());
    }

}
*/
