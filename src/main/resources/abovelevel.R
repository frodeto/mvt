abovelevel <- function(df, colname, level) {
        col <- subset(df, select=c("Matvare", colname))
        col <- col[-1,,drop=FALSE]
        col <- subset(col, !is.na(col[,2]))
        col[,2] <- as.numeric(levels(col[,2])[col[,2]])
        col <- subset(col, col[,2]>level)
        col
}