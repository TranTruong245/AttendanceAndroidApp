package com.example.testing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
public class StudentList extends AppCompatActivity {
    RecyclerView recyclerView;
    DatabaseReference database;
    StudentAdapter studentAdapter;
    ArrayList<Student> list;

    public void onBackPressed() {
        // Quay về màn hình trước đó
        Intent intent = new Intent(this, Select.class);
        startActivity(intent);
        finish(); // Kết thúc hiện tại Activity để ngăn chặn người dùng quay lại nó bằng nút back
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);
        recyclerView = findViewById(R.id.studentList);
        database = FirebaseDatabase.getInstance().getReference("users");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        studentAdapter = new StudentAdapter(this,list);
        recyclerView.setAdapter(studentAdapter);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){

                    Student student = dataSnapshot.getValue(Student.class);
                    list.add(student);


                }
                studentAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}