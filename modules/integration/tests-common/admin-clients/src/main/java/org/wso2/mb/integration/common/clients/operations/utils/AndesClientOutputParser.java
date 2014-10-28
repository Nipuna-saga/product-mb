/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.mb.integration.common.clients.operations.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class AndesClientOutputParser {

    private static Log log = LogFactory.getLog(AndesClientOutputParser.class);

    private Map<Long, Integer> mapOfReceivedMessages = new HashMap<Long, Integer>();
    private List<Long> messages = new ArrayList<Long>();
    private String filePath = "";


    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public AndesClientOutputParser(String filePath) {
        this.filePath = filePath;
        parseFile();
    }

    public void parseFile() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            try {
                String line = br.readLine();

                while (line != null) {
                    String[] infoParts = line.split("-");
                    String messageIdentifierAsString = infoParts[1];
                    long messageIdentifier = Long.parseLong(messageIdentifierAsString);
                    addMessage(messageIdentifier);
                    line = br.readLine();
                }
            } finally {
                br.close();
            }
        } catch (Exception e) {
            log.error("Error while parsing the file containing received messages", e);
        }
    }

    /**
     * Check whether messages are duplicated.
     * Returns duplicated ids
     *
     * @return
     */
    public Map<Long, Integer> checkIfMessagesAreDuplicated() {
        Map<Long, Integer> messagesDuplicated = new HashMap<Long, Integer>();
        for (Long messageIdentifier : mapOfReceivedMessages.keySet()) {
            if (mapOfReceivedMessages.get(messageIdentifier) > 1) {
                messagesDuplicated.put(messageIdentifier, mapOfReceivedMessages.get(messageIdentifier));
            }
        }
        return messagesDuplicated;
    }

    public boolean checkIfMessagesAreInOrder() {
        boolean result = true;
        for (int count = 0; count < messages.size(); count++) {
            if (messages.get(count) != (count)) {
                result = false;
                log.warn("Message order is broken at message " + messages.get(count));
                break;
            }
        }
        return result;
    }

    public void printMissingMessages(int numberOfSentMessages) {
        log.info("Printing Missing Messages");
        for (long count = 0; count < numberOfSentMessages; count++) {
            if (mapOfReceivedMessages.get(count) == null) {
                log.info("missing message id:" + count + 1 + "\n");
            }
        }
    }

    public void printDuplicateMessages() {
        log.info("Printing Duplicated Messages");
        printMap(checkIfMessagesAreDuplicated());
    }

    public void printMessagesMap() {
        log.info("Printing Received Messages");
        printMap(mapOfReceivedMessages);
    }

    public void clearFile() {
        File file = new File(filePath);
        if (file.delete()) {
            log.info("File at " + filePath + " is removed...");
        }
    }

    private void addMessage(Long messageIdentifier) {
        if (mapOfReceivedMessages.get(messageIdentifier) == null) {
            mapOfReceivedMessages.put(messageIdentifier, 1);
        } else {
            int currentCount = mapOfReceivedMessages.get(messageIdentifier);
            mapOfReceivedMessages.put(messageIdentifier, currentCount + 1);
        }

        messages.add(messageIdentifier);
    }

    private void printMap(Map<Long, Integer> messageMap) {
        for (Long messageIdentifier : messageMap.keySet()) {
            log.info(messageIdentifier + "-----" + messageMap.get(messageIdentifier));
        }
    }

    public void printMessagesSorted() {
        log.info("Printing Sorted Messages");
        List<Long> cloneOfMessages = new ArrayList<Long>();
        cloneOfMessages.addAll(messages);
        Collections.sort(cloneOfMessages);
        for (int count = 0; count < cloneOfMessages.size(); count++) {
            log.info(cloneOfMessages.get(count) + "\n");
        }
    }

    public boolean transactedOperations(long operationOccurredIndex) {

        boolean result = false;
        int count = 0;
        long firstMessageIdentifier = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            try {
                String line = br.readLine();

                while (line != null) {
                    String[] infoParts = line.split("-");
                    String messageIdentifierAsString = infoParts[1];
                    long messageIdentifier = Long.parseLong(messageIdentifierAsString);
                    if (count == 0) {
                        firstMessageIdentifier = messageIdentifier;
                    }

                    if (count == (operationOccurredIndex)) {
                        if (messageIdentifier == firstMessageIdentifier) {

                            result = true;
                        }
                    }

                    line = br.readLine();
                    count++;
                }
            } finally {
                br.close();
            }
        } catch (Exception e) {
            log.error("Error while parsing the file containing received messages" , e);
        }

        org.wso2.mb.integration.common.clients.operations.utils.AndesClientUtils.flushPrintWriter();
        return result;
    }


    public int numberDuplicatedMessages() {
        int duplicateCount = 0;
        List<Long> messagesDuplicated = new ArrayList<Long>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            try {
                String line = br.readLine();

                while (line != null) {
                    String[] infoParts = line.split("-");
                    String messageIdentifierAsString = infoParts[1];
                    long messageIdentifier = Long.parseLong(messageIdentifierAsString);

                    if (messagesDuplicated.contains(messageIdentifier)) {

                        duplicateCount++;
                    } else {
                        messagesDuplicated.add(messageIdentifier);

                    }


                    line = br.readLine();

                }
            } finally {
                br.close();
            }
        } catch (Exception e) {
            log.error("Error while parsing the file containing received messages" , e);
        }


        return duplicateCount;
    }
}
