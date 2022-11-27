package com.ransankul.chatapp.encryptAnddecrypt;

import android.util.Log;

public class binary {
    public static String encode(String s) {
        // create a string to add in the initial
        // binary code for extra security
        String ini = "11111111";
        int cu = 0;

        // create an array
        int arr[] = new int[11111111];

        // iterate through the string
        for (int i = 0; i < s.length(); i++) {
            // put the ascii value of
            // each character in the array
            arr[i] = (int) s.charAt(i);
            cu++;
        }
        String res = "";

        // create another array
        int bin[] = new int[111];
        int idx = 0;

        // run a loop of the size of string
        for (int i1 = 0; i1 < cu; i1++) {

            // get the ascii value at position
            // i1 from the first array
            int temp = arr[i1];

            // run the second nested loop of same size
            // and set 0 value in the second array
            for (int j = 0; j < cu; j++) bin[j] = 0;
            idx = 0;

            // run a while for temp > 0
            while (temp > 0) {
                // store the temp module
                // of 2 in the 2nd array
                bin[idx++] = temp % 2;
                temp = temp / 2;
            }
            String dig = "";
            String temps;

            // run a loop of size 7
            for (int j = 0; j < 7; j++) {

                // convert the integer to string
                temps = Integer.toString(bin[j]);

                // add the string using
                // concatenation function
                dig = dig.concat(temps);
            }
            String revs = "";

            // reverse the string
            for (int j = dig.length() - 1; j >= 0; j--) {
                char ca = dig.charAt(j);
                revs = revs.concat(String.valueOf(ca));
            }
            res = res.concat(revs);
        }
        // add the extra string to the binary code
        res = ini.concat(res);

        // return the encrypted code
        return res;
    }

    public static String decode(String s) {
        String invalid = "Invalid Code";

        // create the same initial
        // string as in encode class
        String ini = "11111111";
        Boolean flag = true;

        // run a loop of size 8
        for (int i = 0; i < 8; i++) {
            // check if the initial value is same
            if (ini.charAt(i) != s.charAt(i)) {
                flag = false;
                break;
            }
        }
        String val = "";

        // reverse the encrypted code
        for (int i = 8; i < s.length(); i++) {
            char ch = s.charAt(i);
            val = val.concat(String.valueOf(ch));
        }

        // create a 2 dimensional array
        int arr[][] = new int[11101][8];
        int ind1 = -1;
        int ind2 = 0;

        // run a loop of size of the encrypted code
        for (int i = 0; i < val.length(); i++) {

            // check if the position of the
            // string if divisible by 7
            if (i % 7 == 0) {
                // start the value in other
                // column of the 2D array
                ind1++;
                ind2 = 0;
                char ch = val.charAt(i);
                arr[ind1][ind2] = ch - '0';
                ind2++;
            } else {
                // otherwise store the value
                // in the same column
                char ch = val.charAt(i);
                arr[ind1][ind2] = ch - '0';
                ind2++;
            }
        }
        // create an array
        int num[] = new int[11111];
        int nind = 0;
        int tem = 0;
        int cu = 0;

        // run a loop of size of the column
        for (int i = 0; i <= ind1; i++) {
            cu = 0;
            tem = 0;
            // convert binary to decimal and add them
            // from each column and store in the array
            for (int j = 6; j >= 0; j--) {
                int tem1 = (int) Math.pow(2, cu);
                tem += (arr[i][j] * tem1);
                cu++;
            }
            num[nind++] = tem;
        }
        String ret = "";
        char ch;
        // convert the decimal ascii number to its
        // char value and add them to form a decrypted
        // string using conception function
        for (int i = 0; i < nind; i++) {
            ch = (char) num[i];
            ret = ret.concat(String.valueOf(ch));
        }
        Log.e("dec", "text 11 - " + ret);

        // check if the encrypted code was
        // generated for this algorithm
        if (val.length() % 7 == 0 && flag == true) {
            // return the decrypted code
            return ret;
        } else {
            // otherwise return an invalid message
            return invalid;
        }
    }

}
