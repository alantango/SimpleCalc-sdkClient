package examples.aws.apig.simpleCalc.sdk.app;

import java.io.IOException;

import org.apache.http.auth.Credentials;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.opensdk.config.ConnectionConfiguration;
import com.amazonaws.opensdk.config.ProxyConfiguration;
import com.amazonaws.opensdk.config.TimeoutConfiguration;

import examples.aws.apig.simpleCalc.sdk.*;
import examples.aws.apig.simpleCalc.sdk.model.*;

public class App {
    SimpleCalcSdk sdkClient;
    boolean isLocal;  // false means running inside ec2 or container
    public App(boolean isLocal) {
        this.isLocal = isLocal;
        initSdk();
    }
    public App() {
        this(true);
    }


    // The configuration settings are for illustration purposes and may not be a
    // recommended best practice.
    private void initSdk() {

        SimpleCalcSdkClientBuilder builder = SimpleCalcSdk.builder()
                .connectionConfiguration(
                        new ConnectionConfiguration()
                                .maxConnections(100)
                                .connectionMaxIdleMillis(10000))
                .timeoutConfiguration(
                        new TimeoutConfiguration()
                                .httpRequestTimeout(300000)
                                .totalExecutionTimeout(300000)
                                .socketTimeout(20000));


        // AWSCredentialsProvider credInstantProfile = InstanceProfileCredentialsProvider.getInstance();
        // AWSCredentials cred = credProvider.getCredentials();
        // System.out.printf("accessKeyId: %s; secret: %s", cred.getAWSAccessKeyId(),
        // cred.getAWSSecretKey());
        // builder.setProxyConfiguration(new
        // ProxyConfiguration().proxyHost("proxyHost").proxyPort(8080));
        // builder.setInstanceProfileCredential()
        AWSCredentialsProvider credProvider = null;
        if(isLocal){
            credProvider = new ProfileCredentialsProvider();
        }else{
            credProvider = InstanceProfileCredentialsProvider.getInstance();
            // builder.setProxyConfiguration(new ProxyConfiguration()
            //                                 .proxyHost("proxyHost")
            //                                 .proxyPort(8080)
            //                                 );
        }

        AWSCredentials cred = credProvider.getCredentials();
        System.out.printf("accessKeyId: %s; secret: %s", cred.getAWSAccessKeyId(), cred.getAWSSecretKey());
    
        builder.setIamCredentials(credProvider);

        sdkClient = builder.build();

    }

    // Calling shutdown is not necessary unless you want to exert explicit control
    // of this resource.
    public void shutdown() {
        sdkClient.shutdown();
    }

    // GetABOpResult getABOp(GetABOpRequest getABOpRequest)
    public Output getResultWithPathParameters(String x, String y, String operator) {
        operator = operator.equals("+") ? "add" : operator;
        operator = operator.equals("/") ? "div" : operator;

        GetABOpResult abopResult = sdkClient.getABOp(new GetABOpRequest().a(x).b(y).op(operator));
        return abopResult.getResult().getOutput();
    }

    public Output getResultWithQueryParameters(String a, String b, String op) {
        GetApiRootResult rootResult = sdkClient.getApiRoot(new GetApiRootRequest().a(a).b(b).op(op));
        return rootResult.getResult().getOutput();
    }

    public Output getResultByPostInputBody(Double x, Double y, String o) {
        PostApiRootResult postResult = sdkClient.postApiRoot(
                new PostApiRootRequest().input(new Input().a(x).b(y).op(o)));
        return postResult.getResult().getOutput();
    }

    public static void main(String[] args) {
        System.out.println("Simple calc starts...");
        String a = args[0];
        String b = args[1];
        String op = args[2];
        boolean isLocal = true;
        if(args.length>3){
            isLocal = Boolean.parseBoolean(args[3]);
        }
        
        // to begin
        App calc = new App(isLocal);
        Output res;


        

        try {

            // call the SimpleCalc API
            // res = calc.getResultWithPathParameters("1", "2", "-");
            // System.out.printf("GET /1/2/-: %s\n", res.getC());

            // Use the type query parameter
            res = calc.getResultWithQueryParameters(a, b, op);
            System.out.printf("GET /?a=" + a + "&b=" + b + "&op=" + op + ": %s\n", res.getC());

            // Call POST with an Input body.
            // res = calc.getResultByPostInputBody(1.0, 2.0, "*");
            // System.out.printf("PUT /\n\n{\"a\":1, \"b\":2,\"op\":\"*\"}\n %s\n",
            // res.getC());
        } catch (Exception ex) {
            System.out.printf("API call failed: %s\n", ex.toString());
        }

    }
}