require'lfs'


local sep = "/"
function renamedir(path)

	for file in lfs.dir(path) do
		if file ~= "." and file ~= ".." then
			local f = path..sep..file
			print ("\t=> "..f.." <=")

			local attr = lfs.attributes (f)
			--assert (type(attr) == "table")
			if attr.mode == "directory" then
				renamedir(f)
			else
				if f:find('-') then
					local newf=path..sep..string.gsub(file, "-", '_')
					print ("\t=> "..newf.." <=")
					cmd = [[ren "]]..f..[[" ]]..[[ "]]..newf..[["]]
					print (cmd)
					os.execute(cmd)
				end
			end
		end
	end
end


renamedir('.')
