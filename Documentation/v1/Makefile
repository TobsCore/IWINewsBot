.PHONY: all clean
all:
	./latexrun ProjektarbeitTelegramBot.tex --latex-cmd=xelatex
	mv ProjektarbeitTelegramBot.pdf Projektarbeit\ -\ Tobias\ Kerst\ \&\ Anna-Lena\ Schwarzkopf.pdf

clean:
	./latexrun --clean-all && rm -rf latex.out
