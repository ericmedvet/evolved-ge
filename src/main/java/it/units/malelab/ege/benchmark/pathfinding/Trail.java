package it.units.malelab.ege.benchmark.pathfinding;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Taken from lib GE and refactored to java
 *
 * @author erikhemberg
 */
public class Trail {

    public static final int GRID_WIDTH = 32;
    public static final int GRID_HEIGHT = 32;
    public static final int EMPTY = 0;
    public static final int FOOD = 1;
    public static final int ANT = 8;
    public int _energy;
    public int _picked_up;
    public int[][] _trail = {
        {0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    public int[][] _working_trail = {
        {0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    public int food;
    public int _current_X, _current_Y, _facing_current_X, _facing_current_Y;

    public Trail() {
        initGEtrail(600);
    }

    public Trail(int energy) {
        initGEtrail(energy);
    }

    public int get_Energy() {
        return this._energy;
    }

    public boolean get_Energy_Left() {
        return this._energy > 0;
    }

    public int getFood() {
        return this.food;
    }

    public int get_Picked_Up() {
        return this._picked_up;
    }

    public double getFitness() {
        return (double) (this.getFood() - this.get_Picked_Up());
    }

    void initGEtrail(int e) {
        _current_X = 0;
        _current_Y = 0;
        _facing_current_X = 0;//1 TODO:make sure this is correct
        _facing_current_Y = 1;//0
        _energy = e;
        _picked_up = 0;
        food = 89;
    }

    @SuppressWarnings({"ConstantConditions", "IOResourceOpenedButNotSafelyClosed"})
    void readTrailGEtrail(String file_name) {
        int y = 0;
        int x;
        char ch;
        int bufferSize = 1024;
        String line;
        try {
            FileReader fr = new FileReader(file_name);
            BufferedReader br = new BufferedReader(fr, bufferSize);
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    for (x = 0; x < line.length(); x++) {
                        ch = line.charAt(x);
                        if (ch == '.' || ch == '0') {
                            _trail[x][y] = EMPTY;
                            _working_trail[x][y] = EMPTY;
                        } else if (ch == '1') {
                            _trail[x][y] = FOOD;
                            _working_trail[x][y] = FOOD;
                        }
                    }
                    y++;
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + file_name);
        } catch (IOException e) {
            System.err.println("IOException: " + file_name);
        }

        for (food = 0, y = 0; y < GRID_HEIGHT; y++) {
            for (x = 0; x < GRID_WIDTH; x++) {
                if (_trail[x][y] == FOOD) {
                    food++;
                }
            }
        }
    }

    public void left() {
        //System.out.print("r");
        if (get_Energy_Left()) {
            _energy--;
            if (_facing_current_Y < 0) {
                _facing_current_X = _current_X + 1;
                _facing_current_Y = _current_Y;
            } else if (_facing_current_Y > GRID_HEIGHT) {
                _facing_current_X = _current_X - 1;
                _facing_current_Y = _current_Y;
            } else if (_facing_current_X < 0) {
                _facing_current_Y = _current_Y - 1;
                _facing_current_X = _current_X;
            } else if (_facing_current_X > GRID_WIDTH) {
                _facing_current_Y = _current_Y + 1;
                _facing_current_X = _current_X;
            } else if (_facing_current_Y < _current_Y) {
                _facing_current_X = _current_X + 1;
                _facing_current_Y = _current_Y;
            } else if (_facing_current_Y > _current_Y) {
                _facing_current_X = _current_X - 1;
                _facing_current_Y = _current_Y;
            } else if (_facing_current_X < _current_X) {
                _facing_current_X = _current_X;
                _facing_current_Y = _current_Y - 1;
            } else if (_facing_current_X > _current_X) {
                _facing_current_X = _current_X;
                _facing_current_Y = _current_Y + 1;
            }
        }
    }

    public void right() {
        if (get_Energy_Left()) {
            //System.out.print("l");
            _energy--;
            if (_facing_current_Y < 0) {
                _facing_current_X = _current_X - 1;
                _facing_current_Y = _current_Y;
            } else if (_facing_current_Y > GRID_HEIGHT) {
                _facing_current_X = _current_X + 1;
                _facing_current_Y = _current_Y;
            } else if (_facing_current_X < 0) {
                _facing_current_Y = _current_Y + 1;
                _facing_current_X = _current_X;
            } else if (_facing_current_X > GRID_WIDTH) {
                _facing_current_Y = _current_Y - 1;
                _facing_current_X = _current_X;
            } else if (_facing_current_Y < _current_Y) {
                _facing_current_X = _current_X - 1;
                _facing_current_Y = _current_Y;
            } else if (_facing_current_Y > _current_Y) {
                _facing_current_X = _current_X + 1;
                _facing_current_Y = _current_Y;
            } else if (_facing_current_X < _current_X) {
                _facing_current_X = _current_X;
                _facing_current_Y = _current_Y + 1;
            } else if (_facing_current_X > _current_X) {
                _facing_current_X = _current_X;
                _facing_current_Y = _current_Y - 1;
            }
        }
    }

    public void move() {
        if (get_Energy_Left()) {
            //System.out.print("m");
            int old_current_X, old_current_Y;
            old_current_X = _current_X;
            old_current_Y = _current_Y;
            _energy--;
            if ((_facing_current_X < GRID_WIDTH) && !(_facing_current_X < 0) && (_facing_current_Y < GRID_HEIGHT) && !(_facing_current_Y < 0)) {
                _current_X = _facing_current_X;
                _current_Y = _facing_current_Y;
                if (_working_trail[_current_X][_current_Y] == 1) {
                    //System.out.print("P");
                    _picked_up++;
                    _working_trail[_current_X][_current_Y] = 0;
                }
                if (old_current_X < _current_X) {
                    _facing_current_X = _current_X + 1;
                    _facing_current_Y = _current_Y;
                }
                if (old_current_X > _current_X) {
                    _facing_current_X = _current_X - 1;
                    _facing_current_Y = _current_Y;
                }
                if (old_current_Y < _current_Y) {
                    _facing_current_Y = _current_Y + 1;
                    _facing_current_X = _current_X;
                }
                if (old_current_Y > _current_Y) {
                    _facing_current_Y = _current_Y - 1;
                    _facing_current_X = _current_X;
                }
            } else {
                if (_facing_current_X > GRID_WIDTH - 1) {
                    _current_X = 0;
                    _facing_current_X = 1;
                } else if (_facing_current_X < 0) {
                    _current_X = GRID_WIDTH - 1;
                    _facing_current_X = GRID_WIDTH - 2;
                } else if (_facing_current_Y > GRID_HEIGHT - 1) {
                    _current_Y = 0;
                    _facing_current_Y = 1;
                } else if (_facing_current_Y < 0) {
                    _current_Y = GRID_HEIGHT - 1;
                    _facing_current_Y = GRID_HEIGHT - 2;
                }
                if (_working_trail[_current_X][_current_Y] == 1) {
                    //System.out.print("P");
                    _picked_up++;
                    _working_trail[_current_X][_current_Y] = 0;
                }
            }
            _working_trail[_current_X][_current_Y] = 8;
        }
    }

    public int food_ahead() {
        int is_there = 0;
        if ((_facing_current_X < GRID_WIDTH) && !(_facing_current_X < 0) && (_facing_current_Y < GRID_HEIGHT) && !(_facing_current_Y < 0)) {
            if (_working_trail[_facing_current_X][_facing_current_Y] == 1) {
                is_there = 1;
            } else {
                is_there = 0;
            }
        } else if (_facing_current_X > GRID_WIDTH - 1) {
            if (_working_trail[0][_current_Y] == 1) {
                is_there = 1;
            } else {
                is_there = 0;
            }
        } else if (_facing_current_X < 0) {
            if (_working_trail[GRID_WIDTH - 1][_current_Y] == 1) {
                is_there = 1;
            } else {
                is_there = 0;
            }
        } else if (_facing_current_Y > GRID_HEIGHT - 1) {
            if (_working_trail[_current_X][0] == 1) {
                is_there = 1;
            } else {
                is_there = 0;
            }
        } else if (_facing_current_Y < 0) {
            if (_working_trail[_current_X][GRID_HEIGHT - 1] == 1) {
                is_there = 1;
            } else {
                is_there = 0;
            }
        }
        return is_there;
    }
}
