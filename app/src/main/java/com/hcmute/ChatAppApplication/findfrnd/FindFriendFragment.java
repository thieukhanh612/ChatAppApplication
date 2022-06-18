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

//declare fragment for find friend
public class FindFriendFragment extends Fragment {
    //declare custom progress bar view
    private View customPb;
    //declare text view for empty
    private TextView emptyFrndtv;
    //declare view for recycle
    private RecyclerView recyclerView;
    //declare adapter for find friend
    private FindFriendAdapter adapter;
    //declare firebase authorization
    private FirebaseAuth mAuth;
    //declare list for friends
    private ArrayList<FindFriendModel> frnds = new ArrayList<>();
    //declare current firebase user
    private FirebaseUser currentUser;
    //declare reference of database
    private DatabaseReference mReference,referenceFriendRequest;

    //initiate fragment
    public FindFriendFragment() {
        // Required empty public constructor
    }
    //initiate fragment for find friend
    public static FindFriendFragment newInstance(String param1, String param2) {
        FindFriendFragment fragment = new FindFriendFragment();
        return fragment;
    }

    @Override
    //action when create view
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_friend, container, false);
    }

    @Override
    //action when view created
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
        //set event to query value
        query.addValueEventListener(new ValueEventListener() {
            @Override
            //action when value change
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                frnds.clear();
                emptyFrndtv.setVisibility(View.GONE);
                for(DataSnapshot ds: snapshot.getChildren()){
                    String userId = ds.getKey();


                    if(!userId.equals(currentUser.getUid())){
                        if(ds.child(NodeNames.NAME).getValue()!= null){

                            final String name = ds.child(NodeNames.NAME).getValue().toString();
                            final String photo = ds.child(NodeNames.PHOTO).toString();
                            //set event for reference of fr request
                            referenceFriendRequest.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                //action when data change
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
                                //action when get an error from database
                                public void onCancelled(@NonNull DatabaseError error) {
                                    customPb.setVisibility(View.GONE);
                                }
                            });

                        }
                    }

                }
            }

            @Override
            //action when get an error from database
            public void onCancelled(@NonNull DatabaseError error) {
                //emptyFrndtv.setVisibility(View.GONE);
                customPb.setVisibility(View.GONE);
                Toast.makeText(getContext(), getContext().getString(R.string.error_frnd, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });



    }
}