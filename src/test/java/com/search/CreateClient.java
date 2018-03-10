package com.search;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateClient{
    @Autowired
    private TestRestTemplate restTemplate;
    @Test
    public void createClient() {
        ResponseEntity<Client> responseEntity =
            restTemplate.postForEntity("/clients", new CreateClientRequest("Foo"), Client.class);
        Client client = responseEntity.getBody();
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals("Foo", client.getName());
    }
}