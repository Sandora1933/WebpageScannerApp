package com.example.webpagescannerapp.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.example.webpagescannerapp.other.NumericKeyBoardTransformationMethod;
import com.example.webpagescannerapp.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    EditText urlEditText, textEditText, maxPageNumberEditText;
    NumberPicker threadsNumberPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        initViews();

        setUpMaxPageNumberEditText();
        setUpNumberPicker();
    }

    public void initViews(){
        urlEditText = findViewById(R.id.urlEditText);
        textEditText = findViewById(R.id.textEditText);
        maxPageNumberEditText = findViewById(R.id.maxPageNumberEditText);
        threadsNumberPicker = findViewById(R.id.threadsNumberPicker);
    }

    public void setUpMaxPageNumberEditText(){
        maxPageNumberEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        maxPageNumberEditText.setTransformationMethod(new NumericKeyBoardTransformationMethod());
    }

    public void setUpNumberPicker(){
        threadsNumberPicker.setMinValue(1);
        threadsNumberPicker.setMaxValue(64);
        threadsNumberPicker.setValue(1);
        threadsNumberPicker.setWrapSelectorWheel(false);
        threadsNumberPicker.setTextColor(getResources().getColor(R.color.colorWhite));
    }

    public void searchButtonClicked(View view) {

        if (isAllFieldsValid()){
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            intent.putExtra("url", urlEditText.getText().toString());
            intent.putExtra("text", textEditText.getText().toString());
            intent.putExtra("max_pages_number", maxPageNumberEditText.getText().toString());
            intent.putExtra("threads_number", threadsNumberPicker.getValue());
            startActivity(intent);
        }

    }

    // Validation for Url field
    public boolean isUrlValid(){
        Pattern pattern = Pattern.compile("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        Matcher match = pattern.matcher(urlEditText.getText().toString());

        if (match.matches()){
            urlEditText.setBackgroundResource(R.drawable.drawable_edit_text_valid);
            return true;
        }
        else {
            urlEditText.setBackgroundResource(R.drawable.drawable_edit_text_invalid);
            Toast.makeText(this, "Enter correct url please", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Validation for Text field
    public boolean isTextForSearchValid(){
        if (textEditText.getText().toString().length() > 0){
            textEditText.setBackgroundResource(R.drawable.drawable_edit_text_valid);
            return true;
        }
        else {
            textEditText.setBackgroundResource(R.drawable.drawable_edit_text_invalid);
            Toast.makeText(this, "Enter correct text please", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Validation for Max pages field
    public boolean isMaxPagesNumberValid(){
        if (maxPageNumberEditText.getText().toString().length() > 0 &&
            Integer.parseInt(maxPageNumberEditText.getText().toString()) > 0){
            maxPageNumberEditText.setBackgroundResource(R.drawable.drawable_edit_text_valid);
            return true;
        }
        else {
            maxPageNumberEditText.setBackgroundResource(R.drawable.drawable_edit_text_invalid);
            Toast.makeText(this, "Enter correct pages-number please", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // General validation
    private boolean isAllFieldsValid(){
        return isUrlValid() && isTextForSearchValid() && isMaxPagesNumberValid();
    }


}