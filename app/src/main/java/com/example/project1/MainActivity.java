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


import org.w3c.dom.Text;

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
    private final int NUM_MINES = 4;
    private int flagsRemaining = NUM_MINES;

    GameStatus gameStatus = GameStatus.NOT_STARTED;
    GameMode gameMode = GameMode.DIGGING;
    Set<Pair> mines;
    Set<Integer> flagged;
    int[][] neighbors = { {-1,-1}, {-1,0}, {-1,1}, {1,-1}, {1,0}, {1,1}, {0,-1}, {0,1} };
    private ArrayList<TextView> cell_tvs;

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

    private void revealMines() {
    // reveal mines
        GridLayout gridLayout = findViewById(R.id.square);
        for (Pair mine : mines) {
            TextView tv = gridLayout.findViewById(mine.id);
            tv.setBackgroundColor(Color.RED);
        }
    }
    private void gameOver() {
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
                button.setText(R.string.flag);
                break;
            case FLAGGING:
                gameMode = GameMode.DIGGING;
                button.setText(R.string.pick);
                break;
        }
    }

    private boolean withinBounds(int i, int j) {
//        TextView tv = findViewById(i*12 + j);


        return 0 <= i && i < ROW_COUNT && 0 <= j && j < COLUMN_COUNT;
    }

    private int getNumAdjacentMines(int i, int j) {
        int adjacentMines = 0;
        for (int[] neighbor : neighbors) {
            Pair explore = new Pair(neighbor[0] + i, neighbor[1] + j);

            if (mines.contains(explore)) {
                adjacentMines++;
            }
        }
        return adjacentMines;
    }

    private void setMines(int i, int j) {
        int adjacentMines = getNumAdjacentMines(i,j);
        TextView v = findViewById(i * 12 + j);
        v.setBackgroundColor(Color.DKGRAY);

        if (adjacentMines > 0) {
            v.setTextColor(Color.WHITE);
            v.setText(Integer.toString(adjacentMines));
            checkForWin();
            return;
        }

        // if adjacent mines is 0, continue
        Queue<Pair> queue = new LinkedList<>();
        queue.add(new Pair(i,j));
        Set<Integer> visited = new HashSet<>();
        visited.add(i * 12 + j);

        while (! queue.isEmpty()) {
            Pair currNode = queue.peek();
            queue.remove();

            for (int[] neighbor : neighbors) {
                int neighborID = (neighbor[0] + currNode.i) * 12 + neighbor[1] + currNode.j;
                if(visited.contains(neighborID) || flagged.contains(neighborID) || !withinBounds(neighbor[0] + currNode.i, neighbor[1] + currNode.j)) {
                    continue;
                }

                Pair explore = new Pair(neighbor[0] + currNode.i, neighbor[1] + currNode.j);
                adjacentMines = getNumAdjacentMines(explore.i, explore.j);

                if (adjacentMines > 0) {
                    v = findViewById(explore.i * 12 + explore.j);
                    v.setBackgroundColor(Color.DKGRAY);
                    v.setTextColor(Color.WHITE);
                    v.setText(Integer.toString(adjacentMines));
                    checkForWin();
                    continue;
                }

                visited.add(neighborID);
                queue.add(explore);

                TextView currView = findViewById(neighborID);
                currView.setBackgroundColor(Color.DKGRAY);
            }
        }
    }

    public void onClickTV(View view) {
        if (Objects.requireNonNull(gameStatus) == GameStatus.NOT_STARTED) {
            gameStatus = GameStatus.STARTED;
        }
        // game over
        else if(Objects.requireNonNull(gameStatus) == GameStatus.GAME_LOST || Objects.requireNonNull(gameStatus) == GameStatus.GAME_WON) {
            gameOver();
            return;
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
                    revealMines();
                    return;
                }

                // check for mines adjacent
                setMines(i,j);
                break;
            case FLAGGING:
                if (backgroundColor == Color.GRAY && flagsRemaining > 0) {
                    tv.setBackgroundColor(Color.BLUE);
                    // set flag emoji
                    tv.setText(new String(Character.toChars(0x1F6A9)));
                    flagsRemaining--;
                    flagged.add(i * 12 + j);
                } else if (backgroundColor == Color.BLUE) {
                    tv.setBackgroundColor(Color.GRAY);
                    tv.setText("");
                    flagged.remove(i * 12 + j);
                    flagsRemaining++;
                }

                // change number of flags
                final TextView timeView = findViewById(R.id.mineStatus);
                timeView.setText(String.valueOf(flagsRemaining));
                break;
        }
        checkForWin();
    }

    private void generateNewGame() {
        cell_tvs = new ArrayList<>();
        mines = randomMineGenerator();
        flagged = new HashSet<>();

        Button button = (Button) findViewById(R.id.modeSwitch);
        button.setOnClickListener(this::onButtonClick);
        if(gameMode == GameMode.DIGGING) {
            button.setText(R.string.pick);
        } else {
            button.setText(R.string.flag);
        }

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

                String time = String.format("%02d:%02d", minutes, seconds);

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

    private void checkForWin() {
        if (flagsRemaining != 0) {
            return;
        }

        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                TextView tv = findViewById(i*12 + j);
                Drawable backgroundDrawable = tv.getBackground();
                int backgroundColor = ((ColorDrawable) backgroundDrawable).getColor();
                if(backgroundColor == Color.GRAY) {
                    return;
                }
            }
        }

        gameStatus = GameStatus.GAME_WON;
        revealMines();
//        gameOver();
    }
}