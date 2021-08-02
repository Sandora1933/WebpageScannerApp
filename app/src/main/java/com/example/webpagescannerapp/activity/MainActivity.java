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

import com.example.webpagescannerapp.databinding.ActivityMainBinding;
import com.example.webpagescannerapp.other.NumericKeyBoardTransformationMethod;
import com.example.webpagescannerapp.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    // View binding class
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //setContentView(R.layout.activity_main);
        setContentView(binding.getRoot());

        setUpMaxPageNumberEditText();
        setUpNumberPicker();
    }

//    public void initViews(){
//        urlEditText = findViewById(R.id.urlEditText);
//        textEditText = findViewById(R.id.textEditText);
//        maxPageNumberEditText = findViewById(R.id.maxPageNumberEditText);
//        threadsNumberPicker = findViewById(R.id.threadsNumberPicker);
//    }

    public void setUpMaxPageNumberEditText(){
        binding.maxPageNumberEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        binding.maxPageNumberEditText.setTransformationMethod(new NumericKeyBoardTransformationMethod());
    }

    public void setUpNumberPicker(){
        binding.threadsNumberPicker.setMinValue(1);
        binding.threadsNumberPicker.setMaxValue(64);
        binding.threadsNumberPicker.setValue(1);
        binding.threadsNumberPicker.setWrapSelectorWheel(false);
        binding.threadsNumberPicker.setTextColor(getResources().getColor(R.color.colorWhite));
    }

    public void searchButtonClicked(View view) {

        if (isAllFieldsValid()){
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            intent.putExtra("url", binding.urlEditText.getText().toString());
            intent.putExtra("text", binding.textEditText.getText().toString());
            intent.putExtra("max_pages_number", binding.maxPageNumberEditText.getText().toString());
            intent.putExtra("threads_number", binding.threadsNumberPicker.getValue());
            startActivity(intent);
        }

    }

    // Validation for Url field
    public boolean isUrlValid(){
        Pattern pattern = Pattern.compile("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        Matcher match = pattern.matcher(binding.urlEditText.getText().toString());

        if (match.matches()){
            binding.urlEditText.setBackgroundResource(R.drawable.drawable_edit_text_valid);
            return true;
        }
        else {
            binding.urlEditText.setBackgroundResource(R.drawable.drawable_edit_text_invalid);
            Toast.makeText(this, "Enter correct url please", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Validation for Text field
    public boolean isTextForSearchValid(){
        if (binding.textEditText.getText().toString().length() > 0){
            binding.textEditText.setBackgroundResource(R.drawable.drawable_edit_text_valid);
            return true;
        }
        else {
            binding.textEditText.setBackgroundResource(R.drawable.drawable_edit_text_invalid);
            Toast.makeText(this, "Enter correct text please", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Validation for Max pages field
    public boolean isMaxPagesNumberValid(){
        if (binding.maxPageNumberEditText.getText().toString().length() > 0 &&
            Integer.parseInt(binding.maxPageNumberEditText.getText().toString()) > 0){
            binding.maxPageNumberEditText.setBackgroundResource(R.drawable.drawable_edit_text_valid);
            return true;
        }
        else {
            binding.maxPageNumberEditText.setBackgroundResource(R.drawable.drawable_edit_text_invalid);
            Toast.makeText(this, "Enter correct pages-number please", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // General validation
    private boolean isAllFieldsValid(){
        return isUrlValid() && isTextForSearchValid() && isMaxPagesNumberValid();
    }


}