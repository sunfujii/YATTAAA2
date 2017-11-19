/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.yattaaa2;

import java.util.Optional;
import java.util.function.Consumer;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.jackson.JacksonFeature;

/**
 * Translate English document to Japanese documents by using Microsoft
 * Translator Text API.
 *
 * @author Yoshio Terada
 */
public class TranslatorTextServices {

    private final static String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    private final static String AUTH_URL = "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";
    private final static String TRANSLATOR_URL = "https://api.microsofttranslator.com/v2/http.svc/Translate";
    private final static String ERROR_MESSAGE = "正しく翻訳ができませんでした。";
    private final static String SUBSCRIPTION_KEY;

    static {
        // mod SunFuji 20171119
        SUBSCRIPTION_KEY = "d***********************************";
    }

    /**
     * Get Access Token from Auth Server.
     *
     * The detail information to get the Auth Token is as follows.
     * http://docs.microsofttranslator.com/oauth-token.html
     *
     * Retrieve your authentication key from the Azure Admin screen. For example
     * accessKey look like following digit value
     * "a2436*********************9bc7e"
     *
     * @return {@code Optional<String>} if
     */
    public Optional<String> getAccessTokenForTranslator() {
        Client client = ClientBuilder.newBuilder()
                .register(JacksonFeature.class)
                .build();
        Entity<String> entity = Entity.entity("", MediaType.TEXT_PLAIN_TYPE);
        Response response = client.target(AUTH_URL)
                .request()
                .header(OCP_APIM_SUBSCRIPTION_KEY, SUBSCRIPTION_KEY)
                .post(entity);
        if (isRequestSuccess(response)) {
            return Optional.of(response.readEntity(String.class));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Translate from English to Japanese.
     *
     * The detail information to translate the document is as follows.
     * http://docs.microsofttranslator.com/text-translate.html
     *
     *
     * @param englishText The text value which you would like to translate.
     * @param accessToken The Key value for authentication
     *
     * @return {@code String} Translated Japanese String
     */
    public String translateEnglish(String englishText, String accessToken) {
        Client client = ClientBuilder.newBuilder()
                .register(JacksonFeature.class)
                .build();

        Response response = client.target(TRANSLATOR_URL)
                .queryParam("text", englishText)
                .queryParam("to", "en")
                .queryParam("contentType", MediaType.TEXT_PLAIN)
                .request()
                .header("Accept", MediaType.APPLICATION_XML)
                .header("Authorization", "Bearer " + accessToken)
                .get();

        if (isRequestSuccess(response)) {
            String readEntity = response.readEntity(String.class);
            readEntity = readEntity.replaceAll("</?" + "string" + "/?>", "");
            readEntity = readEntity.replaceAll("<" + "string" + " [^>]*>", "");

            //            ResponseFromTranslateText unmarshal = JAXB.unmarshal(new StringReader(readEntity), ResponseFromTranslateText.class);
            return readEntity;
        } else {
            return ERROR_MESSAGE;
        }
    }

    private boolean isRequestSuccess(Response response) {
        Response.StatusType statusInfo = response.getStatusInfo();
        Response.Status.Family family = statusInfo.getFamily();
        return family != null && family == Response.Status.Family.SUCCESSFUL;
    }

    public static void main(String[] args) {
        TranslatorTextServices translator = new TranslatorTextServices();
        Optional<String> token = translator.getAccessTokenForTranslator();
        token.ifPresent(s -> {
            String translateEnglish = translator.translateEnglish("こんにちは", s);
            System.out.println(translateEnglish);
        });
    }
}
