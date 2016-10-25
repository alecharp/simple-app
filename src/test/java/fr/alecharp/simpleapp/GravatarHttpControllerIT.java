/*
 * Copyright (c) 2016 Adrien Lecharpentier
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fr.alecharp.simpleapp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GravatarHttpControllerIT {

    @Value("${local.server.port}")
    int port;

    @Test
    public void shouldBeAbleToGetImageURIForEmail() {
        String input = "me@alecharp.fr";
        String expected = "4276d24c5065404aa80586c4a8b275f4";
        int defaultSize = 450;

        given().
            port(port).
            param("email", input).
        when().
            get("/api/img").
        then().
            content(containsString(expected)).
            content(containsString("s=" + defaultSize));
    }

    @Test
    public void shouldBeAbleToTweakImageSize() {
        String input = "me@alecharp.fr";
        String expected = "4276d24c5065404aa80586c4a8b275f4";
        int size = 1024;

        given().
            port(port).
            param("email", input).
            param("size", size).
        when().
            get("/api/img").
        then().
            content(containsString(expected)).
            content(containsString("s=" + size));
    }
}
