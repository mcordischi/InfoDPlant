@echo off
dir /Ad /B | find /V /C "" > dataset.conf
FOR /D %%G in ("*") DO (
echo %%G >> dataset.conf
dir %%G\contours /A-d /B | find /c "." >> dataset.conf
dir %%G\contours /B /S >> dataset.conf
)
echo Done
PAUSE
