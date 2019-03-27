package com.oceanprotocol.squid.request.executors;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

public class BasicExecutor implements Executor {

    private static final Logger log = LogManager.getLogger(BasicExecutor.class);


    @Override
    public void setUp() {

    }

    @Override
    public Boolean executeRequest() throws Exception {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("EXECUTED");
        int randomNum = ThreadLocalRandom.current().nextInt(0,  100);
        if (randomNum % 3 == 0)
            throw new Exception("Divided By 3");
        if (randomNum % 2 == 0)
            return true;

        return false;

    }
}
