package com.example.mynewapp.NavigationMenu.TopUpHistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.mynewapp.HomeActivity2;
import com.example.mynewapp.R;
import com.example.mynewapp.TopUp;
import com.example.mynewapp.TopUpActivity;
import com.example.mynewapp.TopUpHistoryAdapter;
import com.example.mynewapp.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class TopUpHistoryFragment extends Fragment {

    private TopUpHistoryViewModel topUpHistoryViewModel;
    private ListView ListViewTopUpHistory;
    private TopUpHistoryAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private static final String TABLE_USER = "User";
    private String userID;
    private ArrayList<TopUp> topUpList;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        topUpHistoryViewModel =
                new ViewModelProvider(this).get(TopUpHistoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_topup_history, container, false);

        ListViewTopUpHistory = root.findViewById(R.id.list_topup_view);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userID = user.getUid();
        db.collection(TABLE_USER).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    User currentUser = documentSnapshot.toObject(User.class);
                    topUpList = currentUser.getListOfTopUp();
                    Collections.reverse(topUpList);

                    adapter = new TopUpHistoryAdapter(getContext(), R.layout.list_topup_history, topUpList);
                    ListViewTopUpHistory.setAdapter(adapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Fail to load top-up history", Toast.LENGTH_SHORT ).show();
            }
        });


        return root;
    }

}