package com.hcmute.ChatAppApplication.requests;

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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

//declare fragment for received fr request
public class ReceivedFriendRequestFragment extends Fragment {
    //declare view for recycle
    private RecyclerView recyclerView;
    //declare request adapter
    private RequestAdapter adapter;
    //declare list for request model
    private ArrayList<RequestModel> frndReqReceivedList;
    //declare custom progressbar
    private View customPb;
    //declare text view
    private TextView txtView;
    //declare reference for database
    private DatabaseReference databaseReferenceRequests, databaseReferenceUser;
    //declare current user
    private FirebaseUser currentUser;
    //initiate fragment
    public ReceivedFriendRequestFragment() {
        // Required empty public constructor
    }
    //initiate fragment
    public static ReceivedFriendRequestFragment newInstance(String param1, String param2) {
        ReceivedFriendRequestFragment fragment = new ReceivedFriendRequestFragment();

        return fragment;
    }

    @Override
    //action when create class
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    //action when create view
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_received_friend_request, container, false);
    }

    @Override
    //action when view created
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.receivedFrndReq_RecyclerView);
        txtView = view.findViewById(R.id.noRequestTv);
        customPb = view.findViewById(R.id.frndSentReq_progressbarView);

        frndReqReceivedList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new RequestAdapter(getActivity(),frndReqReceivedList);
        recyclerView.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
        databaseReferenceRequests = FirebaseDatabase.getInstance().getReference().child(NodeNames.FRIEND_REQUESTS).child(currentUser.getUid());

        txtView.setVisibility(View.VISIBLE);
        customPb.setVisibility(View.VISIBLE);

        databaseReferenceRequests.addValueEventListener(new ValueEventListener() {
            @Override
            //action when data change
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                txtView.setVisibility(View.GONE);
                frndReqReceivedList.clear();
                customPb.setVisibility(View.GONE);
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(ds.exists()){

                        String requestType =  ds.child(NodeNames.REQUESTS_TYPE).getValue().toString();
                        if(requestType.equals(Constants.FRIEND_REQUESTS_RECEIVED)){

                            String userId = ds.getKey();

                            databaseReferenceUser.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                //action when data change
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    String userName = snapshot.child(NodeNames.NAME).getValue().toString();
                                    String photoFile = "";
                                    if(snapshot.child(NodeNames.PHOTO).getValue()!=null){
                                       photoFile =  snapshot.child(NodeNames.PHOTO).getValue().toString();
                                    }

                                    frndReqReceivedList.add(new RequestModel(userId,userName,photoFile));
                                    adapter.notifyDataSetChanged();
                                    txtView.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);

                                }

                                @Override
                                //action when get error from database
                                public void onCancelled(@NonNull DatabaseError error) {
                                    customPb.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(), getActivity().getString(R.string.failed_to_fetch_accep_request,error.getMessage()), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                    if(frndReqReceivedList.isEmpty()){
                        recyclerView.setVisibility(View.GONE);
                        txtView.setVisibility(View.VISIBLE);
                    }

                }

            }

            @Override
            //action when get error from database
            public void onCancelled(@NonNull DatabaseError error) {

                customPb.setVisibility(View.GONE);
                Toast.makeText(getActivity(), getActivity().getString(R.string.failed_to_fetch_accep_request,error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });

    }
}