package com.example.project1;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Collections;


class Pair {
    // Pair attributes
    public int i;
    public int j;
    public int id;

    // Constructor to initialize pair
    public Pair(int i, int j) {
        // This keyword refers to current instance
        this.i = i;
        this.j = j;
        this.id = i * 12 + j;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + i;
        result = 31 * result + j;
        return result;
    }

    // shallow equals
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair)) {
            return false;
        }
        Pair other = (Pair) obj;
        return other.i == i && other.j == j;
    }

}

public class MainActivity extends AppCompatActivity {
    private final int COLUMN_COUNT = 10;
    private final int ROW_COUNT = 12;
    private int clock = 0;
    private final int NUM_MINES = 50;
    private int flagsRemaining = NUM_MINES;

    GameStatus gameStatus = GameStatus.NOT_STARTED;
    GameMode gameMode = GameMode.DIGGING;
    Set<Pair> mines;
    int[][] neighbors = { {-1,-1}, {-1,0}, {-1,1}, {1,-1}, {1,0}, {1,1}, {0,-1}, {0,1} };

    private ArrayList<TextView> cell_tvs;

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public int getClock() {
        return clock;
    }

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    private void gameOver() {
        // reveal mines
        GridLayout gridLayout = findViewById(R.id.square);
        for (Pair mine: mines) {
            TextView tv = gridLayout.findViewById(mine.id);
            tv.setBackgroundColor(Color.RED);
        }

        // display end screen
        Intent intent = new Intent(this, ResultsActivity.class);
        if( gameStatus == GameStatus.GAME_LOST ) {
            intent.putExtra("result", "You lost. Time taken: " + clock + " Seconds");
        } else {
            intent.putExtra("result", "Used " + clock+ " Seconds. You won!");
        }
        startActivity(intent);
    }

    public void onButtonClick(View view) {
        Button button = findViewById(R.id.modeSwitch);

        switch (gameMode) {
            case DIGGING:
                gameMode = GameMode.FLAGGING;
                button.setText(gameMode.name());
                break;
            case FLAGGING:
                gameMode = GameMode.DIGGING;
                button.setText(gameMode.name());
                break;
        }
    }

    private boolean withinBounds(int i, int j) {
        return 0 <= i && i < ROW_COUNT && 0 <= j && j < COLUMN_COUNT;
    }

    private void setMines(int i, int j) {
        int adjacentMines = 0;
        for (int[] neighbor : neighbors) {
            Pair explore = new Pair(neighbor[0] + i, neighbor[1] + j);

            if (mines.contains(explore)) {
                adjacentMines++;
            }
        }

        if (adjacentMines > 0) {
            TextView v = findViewById(i * 12 + j);
            v.setText(Integer.toString(adjacentMines));
        }
    }

    public void onClickTV(View view) {
        if (Objects.requireNonNull(gameStatus) == GameStatus.NOT_STARTED) {
            gameStatus = GameStatus.STARTED;
        }

        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        int i = n / COLUMN_COUNT;
        int j = n % COLUMN_COUNT;

        // if flagged, do nothing
        Drawable backgroundDrawable = tv.getBackground();
        int backgroundColor = ((ColorDrawable) backgroundDrawable).getColor();

        switch (gameMode) {
            case DIGGING:
                if (backgroundColor == Color.BLUE || backgroundColor == Color.DKGRAY) {
                    return;
                }

                // if mine is selected, game over
                if (mines.contains(new Pair(i,j))) {
                    Log.d("myTag", "MINE SELECTED");
                    tv.setBackgroundColor(Color.RED);
                    gameStatus = GameStatus.GAME_LOST;
                    gameOver();
                    return;
                }

                tv.setTextColor(Color.GREEN);

                // check for mines adjacent
                tv.setBackgroundColor(Color.DKGRAY);

                break;
            case FLAGGING:
                if (backgroundColor == Color.GRAY && flagsRemaining > 0) {
                    tv.setBackgroundColor(Color.BLUE);
                    flagsRemaining--;
                } else if (backgroundColor == Color.BLUE) {
                    tv.setBackgroundColor(Color.GRAY);
                    flagsRemaining++;
                }

                // change number of flags
                final TextView timeView = findViewById(R.id.mineStatus);
                timeView.setText(String.valueOf(flagsRemaining));
                break;
        }
    }

    private void generateNewGame() {
        cell_tvs = new ArrayList<>();
        mines = randomMineGenerator();

        Button button = (Button) findViewById(R.id.modeSwitch);
        button.setOnClickListener(this::onButtonClick);
        button.setText(gameMode.name());

        // display flag count
        final TextView timeView = findViewById(R.id.mineStatus);
        timeView.setText(String.valueOf(flagsRemaining));

        GridLayout grid = findViewById(R.id.square);

        // format each cell of grid
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                TextView tv = new TextView(this);
                tv.setHeight( dpToPixel(24) );
                tv.setWidth( dpToPixel(24) );
                tv.setTextSize(20);
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.GRAY);

                // set id for each cell
                tv.setId(i*12 + j);

                // set listener for each cell
                tv.setOnClickListener(this::onClickTV);
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);
                grid.addView(tv, lp);
                cell_tvs.add(tv);
            }
        }
        // start timer
        runTimer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generateNewGame();
    }

    // timer logic
    private void runTimer() {
        final TextView timeView = (TextView) findViewById(R.id.timer);

        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours =clock/3600;
                int minutes = (clock%3600) / 60;
                int seconds = clock%60;
                String time = String.format("%d", seconds);

                if (gameStatus == GameStatus.STARTED) {
                    clock++;
                }
                handler.postDelayed(this, 1000);
                timeView.setText(time);
            }
        });
    }

    // random mine generator logic
    private Set<Pair> randomMineGenerator() {
        ArrayList<Pair> coordinates = new ArrayList<>();
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                coordinates.add(new Pair(i, j));
            }
        }
        Collections.shuffle(coordinates);

        // extract mines (first n coordinates from shuffled list)
        Set<Pair> mines = new HashSet<>();
        for (int i = 0; i < NUM_MINES; i++ ) {
            mines.add(coordinates.get(i));
        }

        return mines;
    }
}