package com.example.unforgettable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.unforgettable.Bmob.Bmobhelper;
import com.example.unforgettable.LitepalTable.memoryCardsList;

import java.util.List;

import static android.app.AlertDialog.*;
import static cn.bmob.v3.Bmob.getApplicationContext;
import static org.litepal.LitePalApplication.getContext;

public class CardsRecyclerAdapter extends RecyclerView.Adapter<CardsRecyclerAdapter.ViewHolder>{
    private List<memoryCardsList> myCardsList;
    private Dbhelper dbhelper = new Dbhelper();

    static class ViewHolder extends RecyclerView.ViewHolder{
        private Button delButton;
        private TextView headline;
        private TextView content_text;
        private TextView detail_text;
        private ImageButton starButton;
        private RelativeLayout cardView;

        public ViewHolder(View view){
            super(view);
            headline = view.findViewById(R.id.headline);
            content_text = view.findViewById(R.id.content_text);
            detail_text = view.findViewById(R.id.detail_text);
            //delButton = view.findViewById(R.id.delButton);
            cardView = view.findViewById(R.id.cardView);
            starButton = view.findViewById(R.id.starButton);
        }
    }

    public CardsRecyclerAdapter(List<memoryCardsList> cardsList){
        myCardsList = cardsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position){
        memoryCardsList cardsList = myCardsList.get(holder.getAdapterPosition());
        holder.headline.setText(cardsList.getHeading());
        holder.content_text.setText(cardsList.getContent());
        String str = "第" + cardsList.getRepeatTime() + "次重复";
        holder.detail_text.setText(str);
        if (cardsList.isLike()) {
            Drawable drawable = getApplicationContext().getResources().getDrawable(R.drawable.ic_star_yel);
            holder.starButton.setImageDrawable(drawable);
        }

        //点击删除按钮
//        holder.delButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v){
//                String heading = holder.headline.getText().toString();
//                myCardsList.remove(holder.getAdapterPosition());
//                notifyDataSetChanged();
//                // 数据库删除
//                dbhelper.deleteCard(heading);
//            }
//        });

        // 点击编辑
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String heading = holder.headline.getText().toString();
                Intent intent = new Intent(v.getContext(), EditCardActivity.class);
                intent.putExtra("heading_extra", heading);
                v.getContext().startActivity(intent);
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new Builder(getContext());
                builder.setTitle("确认");
                builder.setMessage("您确定要删除这条记录吗？");
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //showPopMenu(,holder.getAdapterPosition());
                        String heading = holder.headline.getText().toString();
                        myCardsList.remove(holder.getAdapterPosition());
                        notifyDataSetChanged();
                        // 数据库删除
                        dbhelper.deleteCard(heading);
                        Toast.makeText(getContext(), "删除成功", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("取消",null);
                builder.show();

                return true;
            }
        });

        // 点击收藏
        holder.starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String heading = holder.headline.getText().toString();
                boolean like = dbhelper.changeLike(heading);
                // 改按键颜色状态
                if (like) {
                    Drawable drawable = getApplicationContext().getResources().getDrawable(R.drawable.ic_star_yel);
                    holder.starButton.setImageDrawable(drawable);
                }
                else {
                    Drawable drawable = getApplicationContext().getResources().getDrawable(R.drawable.ic_star_black);
                    holder.starButton.setImageDrawable(drawable);
                }
            }
        });
    }

    @Override
    public int getItemCount(){
        return myCardsList.size();
    }
}
