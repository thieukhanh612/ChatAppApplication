package com.hcmute.ChatAppApplication.requests;

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
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestAdapterViewHolder> {


    private Context mContext;
    private ArrayList<RequestModel> receivedFrndRequests;
    private DatabaseReference databaseReference,databaseReferenceChats;
    private FirebaseUser currentUser;

    public RequestAdapter(Context mContext, ArrayList<RequestModel> receivedFrndRequests) {
        this.mContext = mContext;
        this.receivedFrndRequests = receivedFrndRequests;
    }

    @NonNull
    @Override
    public RequestAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.receive_frnd_request_layout, parent,false);
        return new RequestAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapterViewHolder holder, int position) {

        RequestModel requestModel = receivedFrndRequests.get(position);

        holder.getPersonName().setText(requestModel.getUserName());

        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.FRIEND_REQUESTS);
        databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child(NodeNames.CHAT);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.IMAGE_FOLDER+"/"+requestModel.getPhotoFile());
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(mContext)
                        .load(uri)
                        .placeholder(R.drawable.profile_image_default)
                        .error(R.drawable.profile_image_default)
                        .into(holder.getImage());
            }
        });

        holder.getAcceptFriendRequestBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.getReceivedFrndReqPb().setVisibility(View.VISIBLE);
                holder.getDeniedRequestBtn().setVisibility(View.GONE);
                holder.getReceivedFrndReqPb().setVisibility(View.GONE);

                String userId = requestModel.getUserId();

                databaseReferenceChats.child(currentUser.getUid())
                        .child(userId)
                        .child(NodeNames.TIME_STAMP)
                        .setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            databaseReferenceChats.child(userId)
                                    .child(currentUser.getUid())
                                    .child(NodeNames.TIME_STAMP)
                                    .setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        databaseReference.child(currentUser.getUid())
                                                .child(userId)
                                                .child(NodeNames.REQUESTS_TYPE)
                                                .setValue(Constants.FRIEND_REQUESTS_ACCEPTED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    databaseReference.child(userId)
                                                            .child(currentUser.getUid())
                                                            .child(NodeNames.REQUESTS_TYPE)
                                                            .setValue(Constants.FRIEND_REQUESTS_ACCEPTED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){

                                                                String tittle = "New Friend Request Accepted";
                                                                String message = "Friend request Accepted by "+ currentUser.getDisplayName();

                                                                Util.sendNotification(mContext,message,tittle,userId);
                                                                holder.getReceivedFrndReqPb().setVisibility(View.GONE);
                                                                holder.getDeniedRequestBtn().setVisibility(View.VISIBLE);
                                                                holder.getReceivedFrndReqPb().setVisibility(View.VISIBLE);
                                                                notifyItemChanged(holder.getAdapterPosition());

                                                            }else{
                                                                handleException(holder, task.getException());
                                                            }
                                                        }
                                                    });
                                                }else{
                                                    handleException(holder, task.getException());
                                                }

                                            }
                                        });


                                    }else{
                                        handleException(holder, task.getException());
                                    }

                                }
                            });



                        }
                        else{
                            handleException(holder, task.getException());
                        }
                    }
                });


            }
        });


        holder.getDeniedRequestBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.getReceivedFrndReqPb().setVisibility(View.VISIBLE);
                holder.getDeniedRequestBtn().setVisibility(View.GONE);
                holder.getReceivedFrndReqPb().setVisibility(View.GONE);

                String userId = requestModel.getUserId();

                databaseReference.child(currentUser.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            databaseReference.child(userId).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        holder.getReceivedFrndReqPb().setVisibility(View.GONE);
                                        notifyItemRemoved(holder.getAdapterPosition());

                                    }
                                    else {
                                        Toast.makeText(mContext,mContext.getString(R.string.failed_deni_req, task.getException()), Toast.LENGTH_SHORT).show();
                                        holder.getReceivedFrndReqPb().setVisibility(View.GONE);
                                        holder.getDeniedRequestBtn().setVisibility(View.VISIBLE);
                                        holder.getReceivedFrndReqPb().setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                        else {
                            Toast.makeText(mContext,mContext.getString(R.string.failed_deni_req, task.getException()), Toast.LENGTH_SHORT).show();
                            holder.getReceivedFrndReqPb().setVisibility(View.GONE);
                            holder.getDeniedRequestBtn().setVisibility(View.VISIBLE);
                            holder.getReceivedFrndReqPb().setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

    }

    private void handleException(RequestAdapterViewHolder holder, Exception exception) {

        Toast.makeText(mContext, mContext.getString(R.string.failed_to_acceept_request, exception), Toast.LENGTH_SHORT).show();
        holder.getReceivedFrndReqPb().setVisibility(View.GONE);
        holder.getDeniedRequestBtn().setVisibility(View.VISIBLE);
        holder.getReceivedFrndReqPb().setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return receivedFrndRequests.size();
    }

    class RequestAdapterViewHolder extends RecyclerView.ViewHolder{

        private TextView personName;
        private Button acceptFriendRequestBtn, deniedRequestBtn;
        private ProgressBar receivedFrndReqPb;
        private CircleImageView image;

        public RequestAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            personName = itemView.findViewById(R.id.received_frndName_req);
            acceptFriendRequestBtn = itemView.findViewById(R.id.acceptFrndReqBtn);
            deniedRequestBtn =  itemView.findViewById(R.id.denyFrndReqBtn);
            image =  itemView.findViewById(R.id.received_friend_Dp_photo);
            receivedFrndReqPb = itemView.findViewById(R.id.reque_progress_req);

        }

        public TextView getPersonName() {
            return personName;
        }

        public Button getAcceptFriendRequestBtn() {
            return acceptFriendRequestBtn;
        }

        public Button getDeniedRequestBtn() {
            return deniedRequestBtn;
        }

        public ProgressBar getReceivedFrndReqPb() {
            return receivedFrndReqPb;
        }

        public CircleImageView getImage() {
            return image;
        }
    }
}
