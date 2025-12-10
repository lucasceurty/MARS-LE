.data
q1: .asciiz "Start of the ball game!!\n\n"
q2: .asciiz "\nStart of quarter 2!\n\n"
commercial_break: .asciiz "\nCoca-Cola is now buy one get one free!\n\n"
q3: .asciiz "\nStart of quarter 3!\n\n"
q4: .asciiz "\nStart of quarter 4!\n\n"
.text

# Start of quarter 1
break q1
run $t0, 10

set $t1, 35
set $t2, 15
catch $t0, $t1, $t2

set $t1, 20
set $t2, 3
tackle $t0, $t1, $t2

td $t3
pat $t3

relax $t3, $t4, safe_play

#Start of quarter 2
break q2
set $t1, 8
set $t2, 2
set $t4, 10

sprint $t1, $t2
stiffarm $t1, 5
ran $t0, $t1

fg $t3

relax $t3, $t4, safe_play

#Start of Halftime intermission
break commercial_break

#Start of quarter 3
break q3
run $t0, 50

pick $t0

td $t3
twopt $t3
cele

set $t1, 10
flag $t0, $t1


relax $t3, $t4, safe_play

#Start of quater 4
break q4
fumble $t0, 15

shout
hailmary $t3

relax $t3, $t4, safe_play

#End of Game

safe_play:
cele

