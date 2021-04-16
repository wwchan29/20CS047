package com.example.mynewapp.NavigationMenu.PaymentHistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mynewapp.NavigationMenu.PaymentHistory.PaymentHistoryViewModel;
import com.example.mynewapp.Payment;
import com.example.mynewapp.PaymentHistoryAdapter;
import com.example.mynewapp.R;
import com.example.mynewapp.TopUp;
import com.example.mynewapp.TopUpHistoryAdapter;
import com.example.mynewapp.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;

public class PaymentHistoryFragment extends Fragment {

    private PaymentHistoryViewModel paymentHistoryViewModel;
    private ListView ListViewPaymentHistory;
    private PaymentHistoryAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private static final String TABLE_USER = "User";
    private String userID;
    private ArrayList<Payment> paymentList;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        paymentHistoryViewModel =
                new ViewModelProvider(this).get(PaymentHistoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_payment_history, container, false);

        ListViewPaymentHistory = root.findViewById(R.id.list_payment_view);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userID = user.getUid();
        db.collection(TABLE_USER).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    User currentUser = documentSnapshot.toObject(User.class);
                    paymentList = currentUser.getListOfPayment();

                    //sort the list by payment date and time, with the latest display on top
                    Collections.reverse(paymentList);

                    adapter = new PaymentHistoryAdapter(getContext(), R.layout.list_payment_history, paymentList);
                    ListViewPaymentHistory.setAdapter(adapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Fail to load payment history", Toast.LENGTH_SHORT ).show();
            }
        });


        return root;
    }

}