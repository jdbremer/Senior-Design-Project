import git

repo = git.Repo('~/Desktop/seniorDesign/Senior-Design-Project')
repo.remotes.origin.pull()
