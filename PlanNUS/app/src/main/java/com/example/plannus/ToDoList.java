package com.example.plannus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;


public class ToDoList extends AppCompatActivity implements View.OnClickListener {

    private String userID;
    private CollectionReference taskRef;
    private ToDoListAdapter adapter;
    private Button createTask;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);
        initVars();
        setUpRecyclerView();
    }

    public void initVars() {

        sessionManager = SessionManager.get();
        userID = sessionManager.getAuth()
                .getCurrentUser()
                .getUid();

        createTask = findViewById(R.id.createTask);
        createTask.setOnClickListener(this);

        taskRef = sessionManager.getFireStore()
                .collection("Users")
                .document(this.userID)
                .collection("Tasks");
    }

    private void setUpRecyclerView() {
        Query query = taskRef.orderBy("deadLineDate", Query.Direction.ASCENDING)
                .orderBy("deadLineTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<ToDoTask> options = new FirestoreRecyclerOptions.Builder<ToDoTask>()
                .setQuery(query, ToDoTask.class)
                .build();
        adapter = new ToDoListAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.taskListAnnouncements);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(adapter);

        slider().attachToRecyclerView(recyclerView);

    }

    private ItemTouchHelper slider() {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.createTask) {
            startActivity(new Intent(this, AddTaskActivity.class));
        }
    }
}