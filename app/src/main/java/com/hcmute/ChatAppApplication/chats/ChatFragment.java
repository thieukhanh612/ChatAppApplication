package com.hcmute.ChatAppApplication.chats;

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
import com.hcmute.ChatAppApplication.common.NodeNames;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    //initiate view
    private RecyclerView recyclerView;
    //initiate chat view
    private TextView emptChatView;
    //initiate custom view
    private View customPb;
    // declare chat list
    private ArrayList<ChatListModel> chatListModelArrayList;
    // declare chat list adapter
    private ChatListAdapter adapter;
    // declare query to get data from database
    private Query query;
    // declare reference of database
    private DatabaseReference databaseReferenceChats, databaseReferenceUsers;
    // declare event listener
    private ChildEventListener childEventListener;
    // declare user list
    private List<String> usersIdList;

    // initiate chat fragment constructor
    public ChatFragment() {
        // Required empty public constructor
    }
    // initiate chat fragment constructor
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();

        return fragment;
    }
    // create chat fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    // action when create view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }
    // action when view created
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.chatRecyclerView);
        customPb = view.findViewById(R.id.chatRecyclerView);
        emptChatView = view.findViewById(R.id.emtChatTv);

        chatListModelArrayList = new ArrayList<>();
        usersIdList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);


        adapter = new ChatListAdapter(getActivity(), chatListModelArrayList);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
        databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child(NodeNames.CHAT).child(currentUser.getUid());

        query = databaseReferenceChats.orderByChild(NodeNames.TIME_STAMP);
        customPb.setVisibility(View.VISIBLE);
        emptChatView.setVisibility(View.VISIBLE);

        childEventListener = new ChildEventListener() {
            // action when child added
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                customPb.setVisibility(View.GONE);
                emptChatView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                updateList(snapshot,true, snapshot.getKey());
            }
            //action when child changed
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                updateList(snapshot,false, snapshot.getKey());

            }
            //action when child removed
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }
            //action when child moved
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            //action when get an error lead to cancel
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        query.addChildEventListener(childEventListener);

    }

    //action when destroy child
    @Override
    public void onDestroy() {
        super.onDestroy();
        query.removeEventListener(childEventListener);
    }
    // dunc to update list chat
    private void updateList(DataSnapshot dataSnapshot, boolean isNew, String userId){

        final String lastMessage, lastMessageTime,unreadCount;

        lastMessage = dataSnapshot.child(NodeNames.LAST_MESSAGE).getValue()==null?"":
                dataSnapshot.child(NodeNames.LAST_MESSAGE).getValue().toString();

        lastMessageTime = dataSnapshot.child(NodeNames.LAST_MESSAGE_TIME).getValue()==null?"":
                dataSnapshot.child(NodeNames.LAST_MESSAGE_TIME).getValue().toString();

        unreadCount = dataSnapshot.child(NodeNames.UNREAD_COUNT).getValue()==null?
                "0": dataSnapshot.child(NodeNames.UNREAD_COUNT).getValue().toString();

        databaseReferenceUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            //action when user list change
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fullname = snapshot.child(NodeNames.NAME).getValue()!=null?
                        snapshot.child(NodeNames.NAME).getValue().toString():"";

                String photoName = snapshot.child(NodeNames.PHOTO).getValue()!=null?
                        snapshot.child(NodeNames.PHOTO).getValue().toString():"";

                ChatListModel chatListModel = new ChatListModel(userId,fullname,photoName,lastMessage,unreadCount,lastMessageTime);

                if(isNew){
                    chatListModelArrayList.add(chatListModel);
                    usersIdList.add(userId);
                    //adapter.notify();

                }else{
                    int indexOfClickedUser = usersIdList.indexOf(userId);
                    chatListModelArrayList.set(indexOfClickedUser, chatListModel);
                }
                adapter.notifyDataSetChanged();
            }
            //action when get an error from database
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getActivity(), getActivity().getString(R.string.failed_to_fetch_chatlist, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}