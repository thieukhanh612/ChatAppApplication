package com.hcmute.ChatAppApplication.findfrnd;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hcmute.ChatAppApplication.R;
import com.hcmute.ChatAppApplication.common.Constants;
import com.hcmute.ChatAppApplication.common.NodeNames;
import com.hcmute.ChatAppApplication.common.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
//declare class adapter for find friend
public class FindFriendAdapter extends RecyclerView.Adapter<FindFriendAdapter.FindFriendViewHolder> {
    //declare list for find friend model
    private ArrayList<FindFriendModel> frnds;
    //declare context contain view
    private Context context;
    //declare database
    private FirebaseDatabase database;
    //declare current user
    private FirebaseUser currentUser;
    //declare reference for database
    private DatabaseReference databaseReference;
    //declare user id
    private String userId;
    //initiate adapter
    public FindFriendAdapter(ArrayList<FindFriendModel> frnds, Context context) {
        this.frnds = frnds;
        this.context = context;
    }

    @NonNull
    @Override
    //action when create view holder
    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_layout, parent, false);
        return new FindFriendViewHolder(view);
    }

    @Override
    //action when bind view to position
    public void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position) {

        FindFriendModel findFriendModel = frnds.get(position);
        holder.getPersonName().setText(findFriendModel.getPersonName());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.IMAGE_FOLDER+"/"+findFriendModel.getPhotoFileName());
        //set event on get url from database
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            //action when success get url
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.profile_image_default)
                        .error(R.drawable.profile_image_default)
                        .into(holder.getImage());
            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.FRIEND_REQUESTS);


        if(findFriendModel.isRequestHasSent()){

            holder.getCancelRequestBtn().setVisibility(View.VISIBLE);
            holder.getSendFriendRequestBtn().setVisibility(View.GONE);
        }
        else {
            holder.getCancelRequestBtn().setVisibility(View.GONE);
            holder.getSendFriendRequestBtn().setVisibility(View.VISIBLE);
        }
        //set event for button send request
        holder.getSendFriendRequestBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            //action when click button
            public void onClick(View view) {

                holder.getSendFriendRequestBtn().setVisibility(View.GONE);
                holder.getFrndReqPb().setVisibility(View.VISIBLE);

                userId = findFriendModel.getUserId();

                databaseReference.child(currentUser.getUid()).child(userId).child(NodeNames.REQUESTS_TYPE)
                        .setValue(Constants.FRIEND_REQUESTS_SENT).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    //action on complete send request
                    public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                databaseReference.child(userId).child(currentUser.getUid()).child(NodeNames.REQUESTS_TYPE)
                                        .setValue(Constants.FRIEND_REQUESTS_RECEIVED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    //action when friend received request
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            Toast.makeText(context, context.getString(R.string.successfully_sent_request), Toast.LENGTH_SHORT).show();

                                            String tittle = "New Friend Request Arrived";
                                            String message = "Friend sent from"+ currentUser.getDisplayName();

                                            Util.sendNotification(context,message,tittle,userId);

                                            holder.getSendFriendRequestBtn().setVisibility(View.GONE);
                                            holder.getFrndReqPb().setVisibility(View.GONE);
                                            holder.getCancelRequestBtn().setVisibility(View.VISIBLE);

                                        }
                                        else{
                                            Toast.makeText(context, context.getString(R.string.unsuccessfully_sent_request), Toast.LENGTH_SHORT).show();

                                            holder.getSendFriendRequestBtn().setVisibility(View.VISIBLE);
                                            holder.getFrndReqPb().setVisibility(View.GONE);
                                            holder.getCancelRequestBtn().setVisibility(View.GONE);

                                        }
                                    }
                                });

                            }
                            else{
                                Toast.makeText(context, context.getString(R.string.unsuccessfully_sent_request,task.getException()), Toast.LENGTH_SHORT).show();

                                holder.getSendFriendRequestBtn().setVisibility(View.VISIBLE);
                                holder.getFrndReqPb().setVisibility(View.GONE);
                                holder.getCancelRequestBtn().setVisibility(View.GONE);
                            }
                    }
                });

            }
        });

        //set event on cancel request button
        holder.getCancelRequestBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            //action when click button
            public void onClick(View view) {

                holder.getCancelRequestBtn().setVisibility(View.GONE);
                holder.getFrndReqPb().setVisibility(View.VISIBLE);

                userId = findFriendModel.getUserId();

                databaseReference.child(currentUser.getUid()).child(userId).child(NodeNames.REQUESTS_TYPE)
                        .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    //action when complete cancel reques
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            databaseReference.child(userId).child(currentUser.getUid()).child(NodeNames.REQUESTS_TYPE)
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                //action when complete cancel fr request
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                       // Toast.makeText(context, context.getString(R.string.successfully_sent_request), Toast.LENGTH_SHORT).show();

                                        holder.getSendFriendRequestBtn().setVisibility(View.VISIBLE);
                                        holder.getFrndReqPb().setVisibility(View.GONE);
                                        holder.getCancelRequestBtn().setVisibility(View.GONE);

                                    }
                                    else{

                                        //failed to cancel request
                                        Toast.makeText(context, context.getString(R.string.unsuccessfully_sent_request), Toast.LENGTH_SHORT).show();

                                        holder.getSendFriendRequestBtn().setVisibility(View.GONE);
                                        holder.getFrndReqPb().setVisibility(View.GONE);
                                        holder.getCancelRequestBtn().setVisibility(View.VISIBLE);

                                    }
                                }
                            });

                        }
                        else{
                           // Toast.makeText(context, context.getString(R.string.unsuccessfully_sent_request), Toast.LENGTH_SHORT).show();

                            holder.getSendFriendRequestBtn().setVisibility(View.GONE);
                            holder.getFrndReqPb().setVisibility(View.GONE);
                            holder.getCancelRequestBtn().setVisibility(View.VISIBLE);;
                        }
                    }
                });

            }
        });

    }

    @Override
    //func get number of friends
    public int getItemCount() {
        return frnds.size();
    }

    //declare view for find friend
    class FindFriendViewHolder extends RecyclerView.ViewHolder {

        //declare text view for name of user
        private TextView personName;
        //declare button for send and cancel request
        private Button sendFriendRequestBtn, cancelRequestBtn;
        //declare progress bar for friend request
        private ProgressBar frndReqPb;
        //declare image view for user photo
        private CircleImageView image;
        //initiate view for find friend
        public FindFriendViewHolder(@NonNull View itemView) {

            super(itemView);
            personName = itemView.findViewById(R.id.frnd_Name_req);
            sendFriendRequestBtn = itemView.findViewById(R.id.sendFrndReq);
            cancelRequestBtn = itemView.findViewById(R.id.cancelFrndReq);
            image = itemView.findViewById(R.id.friend_Dp);
            frndReqPb = itemView.findViewById(R.id.progress_req);
        }
        //func to get text view for person name
        public TextView getPersonName() {
            return personName;
        }
        //fun to get button send fr request
        public Button getSendFriendRequestBtn() {
            return sendFriendRequestBtn;
        }
        //func to get button cancel fr request
        public Button getCancelRequestBtn() {
            return cancelRequestBtn;
        }
        //func to get progress bar for fr request
        public ProgressBar getFrndReqPb() {
            return frndReqPb;
        }
        //func to get image view for user photo
        public CircleImageView getImage() {
            return image;
        }
    }
}

