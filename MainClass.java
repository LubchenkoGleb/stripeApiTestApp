package lubchenko.stripeApiTestApp;
import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.*;

import java.io.*;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * Created by glebl_000 on 6/18/2016.
 */
public class MainClass {

    public static void main(String[] args) {
        Stripe.apiKey = initializeApiKey("testList.json");
        if(Stripe.apiKey == null)
            return;
        System.out.println("Key api is: " + Stripe.apiKey + "\n---------------------------");
        viewBalance();
        viewCustomerList();

        parseTasksFromJson("testList.json");

        viewBalance();
        viewCustomerList();
    }

    private static void createCard(JSONObject jsonObject) {

        String customerID = (String)jsonObject.get("customerID");
        String number = (String)jsonObject.get("cardNumber");
        String cvc = (String)jsonObject.get("cvc");
        String exp_month = (String)jsonObject.get("exp_month");
        String exp_year = (String)jsonObject.get("exp_year");

        if(customerID == null || number == null || cvc == null || exp_month == null || exp_year == null){
            System.err.println("incorrect json file. Check it");
            return;
        }

        Dictionary cardParams = new Hashtable();
        cardParams.put("object", "card");
        cardParams.put("number", number);
        cardParams.put("cvc", cvc);
        cardParams.put("exp_month", exp_month);
        cardParams.put("exp_year", exp_year);

        Customer customer = null;
        try {
            customer = Customer.retrieve(customerID);
            Map<String, Object> params = new HashMap<String, Object>();

            params.put("source", cardParams);
            customer.getSources().create(params);

        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (CardException e) {
            System.err.println("Your card number is incorrect. Fix it in your json file");
        } catch (APIException e) {
            e.printStackTrace();
        }
    }

    private static void createUser(JSONObject jsonObject){

        String email = (String)jsonObject.get("email");
        if(email == null){
            System.err.println("incorrect json file. Check it");
            return;
        }

        Map<String, Object> customerParams = new HashMap<String, Object>();
        customerParams.put("email", email);

        try {
            Customer.create(customerParams);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (CardException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
        }
    }

    private static void makeCharge(JSONObject jsonObject){

        String customerID = (String)jsonObject.get("customerID");

        String amount = (String)jsonObject.get("amount");
        String currency = (String)jsonObject.get("currency");
        String number = (String)jsonObject.get("cardNumber");
        String cvc = (String)jsonObject.get("cvc");
        String exp_month = (String)jsonObject.get("exp_month");
        String exp_year = (String)jsonObject.get("exp_year");

        if(number == null || cvc == null || exp_month == null || exp_year == null || amount == null || currency == null){
            System.err.println("incorrect json file. Check it");
            return;
        }

        Dictionary cardParams = new Hashtable();
        cardParams.put("object", "card");
        cardParams.put("number", number);
        cardParams.put("cvc", cvc);
        cardParams.put("exp_month", exp_month);
        cardParams.put("exp_year", exp_year);

        Map<String, Object> chargeParams = new HashMap<String, Object>();
        chargeParams.put("amount", amount);
        chargeParams.put("currency", currency);
        chargeParams.put("source", cardParams);

//        //chargeParams.put("source", "tok_18NqHwKsj1aUOFcxUdCqBCu1"); // obtained with Stripe.js
//        chargeParams.put("description", "Charge for test@example.com");
//
        try {
            Charge.create(chargeParams);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (CardException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
        }
    }


    private static String initializeApiKey(String filePath){
        FileReader reader = null;
        try {
            reader = new FileReader(filePath);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            return (String) jsonObject.get("apiKey");
        } catch (Exception e) {
            System.err.println("api key wasn't initialized");
           return null;
        }

    }

    private static void parseTasksFromJson(String filePath){
        try {
            FileReader reader = new FileReader(filePath);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

            JSONArray jsonArray = (JSONArray) jsonObject.get("tasks");
            Iterator jsonArrayIterator = jsonArray.iterator();

            while (jsonArrayIterator.hasNext()) {
                JSONObject currentTask = (JSONObject)jsonArrayIterator.next();
                String type = (String)currentTask.get("type");

                if(type.equals("card"))
                    createCard(currentTask);
                else if(type.equals("customer"))
                    createUser(currentTask);
                else if(type.equals("charge"))
                    makeCharge(currentTask);
                else {
                    System.err.println("ConfigFile incorrect. Check it!");
                }
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private static void viewBalance(){
        Balance balance = null;
        try {
            balance = Balance.retrieve();
            if(balance != null) {
                List<Money> moneys = balance.getPending();
                for (Money money : moneys) {
                    System.out.println("avaliable money on your account: " + ((double)money.getAmount()/100) + " " + money.getCurrency());
                }
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (CardException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
        }



    }

    private static void viewCustomerList(){
        System.out.println("----customers and amount of their cards -----");
        Map<String, Object> customerParams = new HashMap<String, Object>();
        customerParams.put("limit", 10);

        try {
            CustomerCollection list = Customer.list(customerParams);
            for(Customer customer : list.getData()){
                System.out.println(customer.getEmail() + " : " + customer.getSources().getData().size());
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (CardException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
        }
        System.out.println("------------------------   ");
    }
}
