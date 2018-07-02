package com.ecobank.ceps;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.ecobank.ceps.transactions.Payments;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class PaymentActivity extends AppCompatActivity implements OnFocusChangeListener {

    public static String TransactionDate;
    public static EditText customerNumber;
    public static EditText referenceNumber;
    public static EditText amount;
    boolean userTouchedView;
    private DatePicker transactionDate;
    private EditText token;
    private EditText orderNo;
    public final static int QRcodeWidth = 500;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Variables.Theme);   //  Change theme

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        amount = (EditText) findViewById(R.id.etAmount);
        amount.setOnFocusChangeListener(this);
    }

    /*******************************************************************************************/
    //  When Focus on amount field
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            currencyFormatter(amount);
            amount.setSelection(amount.getText().length());
        } else if (!hasFocus)
            userTouchedView = false;
    }

    //  Format Amount field into currency
    public void currencyFormatter(View view) {
        amount = (EditText) findViewById(R.id.etAmount);
        amount.setSelection(amount.getText().length());
        amount.addTextChangedListener(new TextWatcher() {


            private String current = "";

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                amount.setSelection(amount.getText().length());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                amount.setSelection(amount.getText().length());
                if (!s.toString().equals(current)) {
                    amount.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[^0-9]", "");

                    BigDecimal parsed = new BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
                    String formatted = NumberFormat.getCurrencyInstance().format(parsed).substring("R".length()).trim();    // Remove currency symbol

                    current = formatted;
                    amount.setText(formatted);
                    amount.setSelection(formatted.length());

                    amount.addTextChangedListener(this);
                }
            }
        });
    }

    /*******************************************************************************************/
    public void onPaymentRequest(View view) {
        int errors = 0;

        transactionDate = (DatePicker) findViewById(R.id.dpTransactionDate);
        customerNumber = (EditText) findViewById(R.id.etCustomerNumber);
        referenceNumber = (EditText) findViewById(R.id.etReferenceNumber);
        amount = (EditText) findViewById(R.id.etAmount);
        token = (EditText) findViewById(R.id.tokenField);
        orderNo = (EditText) findViewById(R.id.orderNumber);
        String QRData = "";

        if (customerNumber.getText().length() == 0) {
            errors++;
            //customerNumber.setError(getString(R.string.customer_number) + "\n" + getString(R.string.cannot_be_empty));
            customerNumber.setError(Html.fromHtml("<font color='#033b4c'>Account Number Cannot Be Empty</font>"));
        }

        if (referenceNumber.getText().length() == 0) {
            errors++;
            //referenceNumber.setError(getString(R.string.invoice_number) + "\n" + getString(R.string.cannot_be_empty));
            referenceNumber.setError(Html.fromHtml("<font color='#033b4c'>Invoice Number Cannot Be Empty</font>"));
        }

        if (amount.getText().length() == 0) {
            errors++;
            //amount.setError("Amount" + "\n"  + "cannot_be_empty");
            amount.setError(Html.fromHtml("<font color='#033b4c'>Amount Cannot Be Empty</font>"));
        }

        if (errors == 0) {
            try {
                QRData = "AccountNo=" + customerNumber.getText().toString() + ',' +
                        "InvoiceNo=" + referenceNumber.getText().toString() + ',' +
                        "Amount=" + amount.getText().toString();

                bitmap = TextToImageEncode(QRData);



            } catch (WriterException e) {
                Toast.makeText(PaymentActivity.this, "Could Not Create QR Code!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;

        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value, BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null);


        } catch (IllegalArgumentException illegalargumentexception) {
            return null;
        }

        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.QRCodeBlackColor) : getResources().getColor(R.color.QRCodeWhiteColor);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            PaymentActivity.this.startActivity(new Intent(PaymentActivity.this, MainActivity.class));
            PaymentActivity.this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
