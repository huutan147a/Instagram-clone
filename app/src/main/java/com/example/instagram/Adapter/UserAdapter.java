package com.example.instagram.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;


import com.example.instagram.Fragment.ProfileFragment;
import com.example.instagram.MainActivity;
import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{
    private Context mContext;
    private List<User> mUsers;
    private boolean isfragment;

    private FirebaseUser firebaseUser;

    public UserAdapter(Context mContext, List<User> mUsers, boolean isfragment){
        this.mUsers= mUsers;
        this.mContext=mContext;
        this.isfragment=isfragment;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final User user = mUsers.get(position);
        viewHolder.btn.setVisibility(View.VISIBLE);
        viewHolder.username.setText(user.getUsername());
        viewHolder.fullname.setText(user.getFullname());
        String hisUID = mUsers.get(position).getId();//hisUID của chatactivity
        /*Picasso là thư viện tải hình ảnh
         //load hình ảnh user trong mục search */
        Picasso.get().load(user.getImageurl()).placeholder(R.drawable.userlogo).into(viewHolder.image_profile);

        isFollowed(user.getId(), viewHolder.btn);
        if (user.getId().equals(firebaseUser.getUid())) {
            viewHolder.btn.setVisibility(View.GONE);
        }
        //trong mục search khi chúng ta nhấp vào người dùng thì sẽ chuyển qua mục thông tin người dùng
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isfragment) {
                    mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("profileid", user.getId()).apply();
                    ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment()).commit();
                }else {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra("publisherid",user.getId());
                    mContext.startActivity(intent);
                }
            }
        });

        //follow and bỏ follow
        viewHolder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.btn.getText().toString().equals("theo dõi")) {
                    FirebaseDatabase.getInstance().getReference().child("Theo dõi").child(firebaseUser.getUid())
                            .child("đang theo dõi").child(user.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Theo dõi").child(user.getId())
                            .child("người theo dõi").child(firebaseUser.getUid()).setValue(true);

                    //tạo thông báo
                    addNotification(user.getId());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Theo dõi").child(firebaseUser.getUid())
                            .child("đang theo dõi").child(user.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Theo dõi").child(user.getId())
                            .child("người theo dõi").child(firebaseUser.getUid()).removeValue();
                }

            }
        });



    }
        //Notification
    private void addNotification(String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid());
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("userid",userid);
        hashMap.put("text", "Đã bắt đầu theo dõi");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap);
    }

    @Override
    public int getItemCount() {
        return  mUsers.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView image_profile;
        public TextView username;
        public TextView fullname;
        public Button btn;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            image_profile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            btn = itemView.findViewById(R.id.btn);

        }
    }
    private void isFollowed(String id, Button btn) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Theo dõi").child(firebaseUser.getUid())
                .child("đang theo dõi");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(id).exists())
                    btn.setText("đang theo dõi");
                else
                    btn.setText("theo dõi");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}


