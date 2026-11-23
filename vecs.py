        answer = {}
        res = [0][0]
        for i in range(len(nums1)):
            answer.update({nums1[i] : 1})
        for i in range(len(nums2)):
            if nums2[i] in answer:
                answer.pop(nums2[i])
            else:
                answer.update({nums2[i] : 2})
        for key, value in answer.items():
            if value == 1:
                res[0].append(key)
            else:
                res[]   

                ans = defaultdict(int)
        cnt = defaultdict(int) 
        for num in arr:
            cnt[num] += 1
            ans[num] = cnt[num]
        print(ans)