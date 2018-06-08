.PHONY: all clean
all:
	./latexrun ProjektarbeitTelegramBot.tex --latex-cmd=xelatex
	mv ProjektarbeitTelegramBot.pdf Projektarbeit\ - \ Geis \&\ Kerst\ \&\ Schwarzkopf.pdf

clean:
	./latexrun --clean-all && rm -rf latex.out
