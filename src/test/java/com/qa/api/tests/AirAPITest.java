package com.qa.api.tests;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import org.json.simple.JSONObject;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Properties;


public class AirAPITest{


    public static String BaseURL;

    public static String API_Key;

    @BeforeMethod

    public void setup() throws IOException {

        Properties prop = new Properties();

        FileInputStream ip = new FileInputStream("src/main/resources/config.properties");
        prop.load(ip);

        RestAssured.baseURI = prop.getProperty("BaseURL");

        API_Key = prop.getProperty("API_Key");
    }


    @BeforeMethod
    @Parameters({"country"})
    public void test_authorization_expectStatus200(String country) {

        Response response = (Response) given().
                when().
                get("/states?country="+country+"&key="+API_Key);
                response.then().
                assertThat().
                //authorization check
                statusCode(200);

        System.out.println("Authorization check:"+response.getStatusCode());
    }

    @Test(priority = 1)
    @Parameters({"country","no_of_states"})
    public void test_numberOfStatesinAustralia(String country,int NumOfStates) {

        Response response = (Response) given().
                when().
                get("/states?country="+country+"&key="+API_Key);
                response.then().
                assertThat().
                //number of states
                body("data.state",hasSize(NumOfStates)).
                and().
                //validate names of states
                 body("data.state[0]",equalTo("ACT")).
                and().
                body("data.state[1]",equalTo("New South Wales")).
                and().
                body("data.state[2]",equalTo("Queensland")).
                and().
                body("data.state[3]",equalTo("South Australia")).
                and().
                body("data.state[4]",equalTo("Tasmania")).
                and().
                body("data.state[5]",equalTo("Victoria")).
                and().
                body("data.state[6]",equalTo("Western Australia"));

        System.out.println("States of "+country+":"+response.asString());
        System.out.println("Number of states in "+country+":"+NumOfStates);
    }

    @Test(priority = 2)

    @Parameters({"lat","lon"})

    public void test_NearestCityAndCoordinates(String lat,String lon) {

        Response response = (Response) given().when().
                contentType("application.json").
                get("/nearest_city?key="+API_Key);

        String coordinates = response.path("data.location.coordinates").toString();

        String city = response.path("data.city").toString();

        System.out.println("City received for given coordinates " + city);

        String co = "["+lon+", "+lat+"]";

        Assert.assertEquals(coordinates,co);
    }

    @Test(priority = 3)

    @Parameters({"lat","lon"})

    public void test_LatitudeAndLongitudeTemperature(String lat,String lon) throws ParseException, IOException {

        JSONParser parser = new JSONParser();

        Reader reader = new FileReader("src/main/resources/sampleJSON.json");

        JSONObject sampleJSON = (JSONObject) parser.parse(reader);

        Response response = (Response) given().when().
                contentType("application.json").
                get("/nearest_city?lat="+lat+"&lon="+lon+"&key="+API_Key);

        String city = response.path("data.city").toString();
        int temp = response.path("data.current.weather.tp");

        System.out.println("City in response for given latitude and longitude:"+city);
        System.out.println("Temperature of "+city+":"+temp);

        //validate the keys

        JSONObject responseJSON = (JSONObject) parser.parse(response.getBody().print());

        Assert.assertTrue(responseJSON.containsKey("status"));
        Assert.assertTrue(responseJSON.containsKey("data"));

        if(responseJSON.containsKey("data")) {
            HashMap data = new HashMap();
            data = (HashMap) responseJSON.get("data");

            Assert.assertTrue(data.containsKey("city"));
            Assert.assertTrue(data.containsKey("state"));
            Assert.assertTrue(data.containsKey("country"));
            Assert.assertTrue(data.containsKey("location"));

            if(data.containsKey("location")) {
                HashMap location = new HashMap();
                location = (HashMap) data.get("location");

                Assert.assertTrue(location.containsKey("type"));
                Assert.assertTrue(location.containsKey("coordinates"));
            }

            Assert.assertTrue(data.containsKey("current"));

            if (data.containsKey("current")) {
                HashMap current = new HashMap();
                current = (HashMap) data.get("current");
                Assert.assertTrue(current.containsKey("weather"));
                Assert.assertTrue(current.containsKey("pollution"));

                if (data.containsKey("weather")) {
                    HashMap weather = new HashMap();
                    weather = (HashMap) data.get("weather");
                    Assert.assertTrue(weather.containsKey("ts"));
                    Assert.assertTrue(weather.containsKey("tp"));
                    Assert.assertTrue(weather.containsKey("pr"));
                    Assert.assertTrue(weather.containsKey("hu"));
                    Assert.assertTrue(weather.containsKey("ws"));
                    Assert.assertTrue(weather.containsKey("wd"));
                    Assert.assertTrue(weather.containsKey("ic"));
                }

                if (data.containsKey("pollution")) {
                    HashMap pollution = new HashMap();
                    pollution = (HashMap) data.get("weather");
                    Assert.assertTrue(pollution.containsKey("ts"));
                    Assert.assertTrue(pollution.containsKey("aqius"));
                    Assert.assertTrue(pollution.containsKey("mainus"));
                    Assert.assertTrue(pollution.containsKey("aqicn"));
                    Assert.assertTrue(pollution.containsKey("maincn"));
                }
            }
        }

    }

}

