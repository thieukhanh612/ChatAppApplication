package com.hcmute.ChatAppApplication.findfrnd;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hcmute.ChatAppApplication.R;
import com.hcmute.ChatAppApplication.common.Constants;
import com.hcmute.ChatAppApplication.common.NodeNames;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class FindFriendFragment extends Fragment {

    private View customPb;
    private TextView emptyFrndtv;
    private RecyclerView recyclerView;
    private FindFriendAdapter adapter;

    private FirebaseAuth mAuth;
    private ArrayList<FindFriendModel> frnds = new ArrayList<>();
    private FirebaseUser currentUser;
    private DatabaseReference mReference,referenceFriendRequest;


    public FindFriendFragment() {
        // Required empty public constructor
    }

    public static FindFriendFragment newInstance(String param1, String param2) {
        FindFriendFragment fragment = new FindFriendFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyFrndtv = view.findViewById(R.id.noRequestTv);
        customPb = view.findViewById(R.id.req_progressbarView);
        recyclerView = view.findViewById(R.id.frndRequestRecyclerView);


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new FindFriendAdapter(frnds, getActivity());
        recyclerView.setAdapter(adapter);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
        referenceFriendRequest = FirebaseDatabase.getInstance().getReference()
                .child(NodeNames.FRIEND_REQUESTS).child(currentUser.getUid());

        emptyFrndtv.setVisibility(View.VISIBLE);
        customPb.setVisibility(View.VISIBLE);

        Query query = mReference.orderByChild(NodeNames.NAME);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                frnds.clear();
                emptyFrndtv.setVisibility(View.GONE);
                for(DataSnapshot ds: snapshot.getChildren()){
                    String userId = ds.getKey();


                    if(!userId.equals(currentUser.getUid())){
                        if(ds.child(NodeNames.NAME).getValue()!= null){

                            final String name = ds.child(NodeNames.NAME).getValue().toString();
                            final String photo = ds.child(NodeNames.PHOTO).toString();

                            referenceFriendRequest.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        String requestType = snapshot.child(NodeNames.REQUESTS_TYPE).getValue().toString();

                                        if(requestType.equals(Constants.FRIEND_REQUESTS_SENT)){

                                            frnds.add(new FindFriendModel(name,photo,userId,true));
                                            adapter.notifyDataSetChanged();
                                            customPb.setVisibility(View.GONE);
                                        }
                                    }
                                    else {
                                        frnds.add(new FindFriendModel(name,photo,userId,false));
                                        adapter.notifyDataSetChanged();
                                        customPb.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    customPb.setVisibility(View.GONE);
                                }
                            });

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //emptyFrndtv.setVisibility(View.GONE);
                customPb.setVisibility(View.GONE);
                Toast.makeText(getContext(), getContext().getString(R.string.error_frnd, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });



    }
}